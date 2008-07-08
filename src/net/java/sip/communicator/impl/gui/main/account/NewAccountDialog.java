package net.java.sip.communicator.impl.gui.main.account;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import net.java.sip.communicator.impl.gui.*;
import net.java.sip.communicator.impl.gui.customcontrols.*;
import net.java.sip.communicator.impl.gui.i18n.*;
import net.java.sip.communicator.impl.gui.utils.*;
import net.java.sip.communicator.service.gui.*;
import net.java.sip.communicator.util.*;

import org.osgi.framework.*;

public class NewAccountDialog
    extends SIPCommDialog
    implements ActionListener
{
    private Logger logger = Logger.getLogger(NewAccountDialog.class);

    private JPanel mainPanel = new JPanel(new BorderLayout(5, 5));

    private JPanel accountPanel = new JPanel(new BorderLayout());

    private JPanel networkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

    private JLabel networkLabel = new JLabel(
        Messages.getI18NString("network").getText());

    private JComboBox networkComboBox = new JComboBox();

    private JButton advancedButton = new JButton(
        Messages.getI18NString("advanced").getText());

    private JButton addAccountButton = new JButton(
        Messages.getI18NString("add").getText());

    private JButton cancelButton = new JButton(
        Messages.getI18NString("cancel").getText());

    private JPanel rightButtonPanel
        = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

    private JPanel buttonPanel = new JPanel(new BorderLayout());

    private String preferredWizardName;

    public NewAccountDialog()
    {
        super(GuiActivator.getUIService().getMainFrame());

        this.setTitle(Messages.getI18NString("newAccount").getText());

        this.getContentPane().add(mainPanel);

        this.mainPanel.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15));

        this.mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        this.buttonPanel.add(advancedButton, BorderLayout.WEST);
        this.buttonPanel.add(rightButtonPanel, BorderLayout.EAST);
        this.advancedButton.addActionListener(this);

        this.rightButtonPanel.add(addAccountButton);
        this.rightButtonPanel.add(cancelButton);
        this.addAccountButton.addActionListener(this);
        this.cancelButton.addActionListener(this);

        this.mainPanel.add(networkPanel, BorderLayout.NORTH);
        this.networkPanel.add(networkLabel, BorderLayout.WEST);
        this.networkPanel.add(networkComboBox, BorderLayout.CENTER);

        this.getRootPane().setDefaultButton(addAccountButton);

        this.networkComboBox.setRenderer(new NetworkListCellRenderer());
        this.networkComboBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                AccountRegistrationWizard wizard
                    = (AccountRegistrationWizard) networkComboBox
                        .getSelectedItem();

                loadSelectedWizard(wizard);
            }
        });

        this.mainPanel.add(accountPanel, BorderLayout.CENTER);

        this.initNetworkList();
    }

    private void initNetworkList()
    {
        // check for preferred wizard
        String prefWName = LoginProperties.getProperty("preferredAccountWizard");
        if(prefWName != null && prefWName.length() > 0)
            preferredWizardName = prefWName;

        ServiceReference[] accountWizardRefs = null;
        try
        {
            accountWizardRefs = GuiActivator.bundleContext
                .getServiceReferences(
                    AccountRegistrationWizard.class.getName(),
                    null);
        }
        catch (InvalidSyntaxException ex)
        {
            // this shouldn't happen since we're providing no parameter string
            // but let's log just in case.
            logger.error(
                "Error while retrieving service refs", ex);
            return;
        }

        // in case we found any, add them in this container.
        if (accountWizardRefs != null)
        {
            logger.debug("Found "
                         + accountWizardRefs.length
                         + " already installed providers.");
            for (int i = 0; i < accountWizardRefs.length; i++)
            {
                AccountRegistrationWizard wizard
                    = (AccountRegistrationWizard) GuiActivator.bundleContext
                        .getService(accountWizardRefs[i]);

                networkComboBox.addItem(wizard);

                // if we have preferred wizard insert it at first position
                if(preferredWizardName != null
                    && wizard.getClass().getName().equals(preferredWizardName))
                    networkComboBox.setSelectedItem(wizard);
            }
        }
    }

    private class NetworkListCellRenderer
        extends JLabel
        implements ListCellRenderer
    {
        public NetworkListCellRenderer()
        {
            this.setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus)
        {
            AccountRegistrationWizard wizard
                = (AccountRegistrationWizard) value;

            if (isSelected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            this.setText(wizard.getProtocolName());
            this.setIcon(new ImageIcon(
                ImageLoader.getBytesInImage(wizard.getIcon())));

            return this;
        }
    }

    private void loadSelectedWizard(AccountRegistrationWizard wizard)
    {
        accountPanel.removeAll();

        JPanel fixedWidthPanel = new JPanel();
        this.accountPanel.add(fixedWidthPanel, BorderLayout.SOUTH);
        fixedWidthPanel.setPreferredSize(new Dimension(430, 3));
        fixedWidthPanel.setMinimumSize(new Dimension(430, 3));
        fixedWidthPanel.setMaximumSize(new Dimension(430, 3));

        accountPanel.add((Component) wizard.getSimpleForm(), BorderLayout.NORTH);
        accountPanel.revalidate();
        accountPanel.repaint();
        this.pack();
    }

    @Override
    protected void close(boolean isEscaped)
    {
    }

    /**
     * 
     */
    public void actionPerformed(ActionEvent event)
    {
        JButton sourceButton = (JButton) event.getSource();

        AccountRegistrationWizard wizard
            = (AccountRegistrationWizard) networkComboBox.getSelectedItem();

        if (sourceButton.equals(advancedButton))
        {
            wizard.setModification(false);

            AccountRegWizardContainerImpl wizardContainer
                = ((AccountRegWizardContainerImpl) GuiActivator.getUIService()
                    .getAccountRegWizardContainer());

            wizardContainer.setTitle(Messages.getI18NString(
                "accountRegistrationWizard").getText());

            wizardContainer.setCurrentWizard(wizard);

            wizardContainer.showDialog(false);
        }
        else if (sourceButton.equals(addAccountButton))
        {
            wizard.signin();

            this.dispose();
        }
        else if (sourceButton.equals(cancelButton))
        {
            this.dispose();
        }
    }
}
