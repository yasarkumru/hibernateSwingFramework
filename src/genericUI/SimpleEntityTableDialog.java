package genericUI;

import hibernate.HibernateManager;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import utils.Entity;
import utils.Utils;
import component.CButton;
import component.CDialog;
import component.TablePanel;

public class SimpleEntityTableDialog extends CDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private Class<? extends Entity> entityClass;
	private TablePanel tablePanel;
	private Entity selectedEntity;
	private JButton okButton;
	private JButton btnGncelle;
	private CButton btnSil;
	private JButton cancelButton;

	/**
	 * Create the dialog.
	 */
	public SimpleEntityTableDialog(Class<? extends Entity> entityClass) {
		this.entityClass = entityClass;
		setBounds(100, 100, 651, 605);
		getContentPane().setLayout(new BorderLayout());
		this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(this.contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			tablePanel = new TablePanel(entityClass.getSimpleName(), entityClass);
			tablePanel.getTable().getSelectionModel()
					.addListSelectionListener(new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting())
								return;
							tablePanelSelected();

						}
					});
			
			tablePanel.getTable().addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2){
						updateClicked();
					}
				}
			});
			contentPanel.add(tablePanel, BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Ekle");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						addClicked();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				btnGncelle = new JButton("Güncelle");
				this.btnGncelle.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						updateClicked();
					}
				});
				btnGncelle.setEnabled(false);
				buttonPane.add(btnGncelle);
			}
			{
				btnSil = new CButton();
				this.btnSil.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						deleteClicked();
					}
				});
				btnSil.setText("Sil");
				btnSil.setEnabled(false);
				buttonPane.add(btnSil);
			}
			{
				cancelButton = new JButton("İptal");
				this.cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelClicked();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		tablePanel.refresh();
	}

	protected void cancelClicked() {
		dispose();
	}

	protected void deleteClicked() {
		int showConfirmMessage = Utils
				.showConfirmMessage("Girdiyi silmek istediğinizden eminmisiniz?");
		if (showConfirmMessage == JOptionPane.YES_OPTION)
			HibernateManager.getInstance().delete(selectedEntity);
	}

	protected void updateClicked() {
		new AddUpdateEntityDialog(entityClass, selectedEntity).setVisible(true);
	}

	protected void tablePanelSelected() {
		selectedEntity = (Entity) tablePanel.getSelectedObject();
		if (selectedEntity == null) {
			btnGncelle.setEnabled(false);
			btnSil.setEnabled(false);
		} else {
			btnGncelle.setEnabled(true);
			btnSil.setEnabled(true);
		}
	}

	protected void addClicked() {
		new AddUpdateEntityDialog(entityClass).setVisible(true);
	}

}
