package genericUI;

import genericUI.utils.ClassDecomposerForUi;
import genericUI.utils.ClassDecomposerForUi.ClassVariable;
import hibernate.HibernateManager;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import utils.Entity;
import utils.Utils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import component.CComboBox;
import component.CDateChooser;
import component.CDialog;
import component.CTextField;

public class AddUpdateEntityDialog extends CDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private List<ClassVariable> classVariables;
	private Class<? extends Entity> entityClass;
	private boolean isNew = true;
	private Entity entity;
	private JButton okButton;

	/**
	 * Create the dialog.
	 * 
	 * @wbp.parser.constructor
	 */
	public AddUpdateEntityDialog(Class<? extends Entity> entityClass) {
		setBounds(100, 100, 426, 587);
		getContentPane().setLayout(new BorderLayout());
		this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(this.contentPanel, BorderLayout.CENTER);
		this.entityClass = entityClass;
		ClassDecomposerForUi.getInstance();
		classVariables = ClassDecomposerForUi.getClassVariables(entityClass);
		

		RowSpec[] rowSpecs = new RowSpec[classVariables.size() * 2];
		for (int i = 0; i < rowSpecs.length; i++) {
			if (i % 2 == 0)
				rowSpecs[i] = FormSpecs.RELATED_GAP_ROWSPEC;
			else
				rowSpecs[i] = FormSpecs.DEFAULT_ROWSPEC;
		}

		contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"), }, rowSpecs));

		for (int i = 0; i < classVariables.size(); i++) {

			JLabel label = new JLabel(classVariables.get(i).getClassName());
			contentPanel.add(label, "2, " + (i * 2 + 2) + ", right, default");

			contentPanel.add(classVariables.get(i).getComponent(), "4, " + (i * 2 + 2)
					+ ", fill, default");
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
						try {
							okClicked();
						} catch (IllegalAccessException e1) {
							e1.printStackTrace();
						} catch (IllegalArgumentException e1) {
							e1.printStackTrace();
						} catch (InvocationTargetException e1) {
							e1.printStackTrace();
						} catch (InstantiationException e1) {
							e1.printStackTrace();
						} catch (ParseException e1) {
							e1.printStackTrace();
							Utils.showErrorMessage("Lütfen rakam giriniz!!!");
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("İptal");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelClicked();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public AddUpdateEntityDialog(Class<? extends Entity> entityClass, Entity selectedEntity) {
		this(entityClass);
		isNew = false;
		entity = selectedEntity;
		okButton.setText("Güncelle");
		for (int i = 0; i < classVariables.size(); i++) {
			try {
				classVariables.get(i).setValue(
						classVariables.get(i).getGetMethod().invoke(selectedEntity));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	protected void okClicked() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException, ParseException {
		for (ClassVariable classVariable : classVariables) {
			if (classVariable.isMandatory()) {
				if (Entity.class.isAssignableFrom(classVariable.getClassType())) {
					CComboBox<?> combobox = (CComboBox<?>) classVariable.getComponent();
					if (combobox.getSelectedObject() == null) {
						Utils.showErrorMessage("Lütfen " + classVariable.getClassName()
								+ " kısmını seçiniz!");
						return;
					}
				} else if (Date.class.isAssignableFrom(classVariable.getClassType())) {
					CDateChooser dateChooser = (CDateChooser) classVariable.getComponent();
					if (dateChooser.getDate() == null) {
						Utils.showErrorMessage("Lütfen " + classVariable.getClassName()
								+ " kısmından tarih seçiniz!");
					}
				}

				else {
					CTextField textField = (CTextField) classVariable.getComponent();
					if (textField.getText().length() == 0) {
						Utils.showErrorMessage("Lütfen " + classVariable.getClassName()
								+ " kısmını doldurunuz!");
						return;
					}
				}
			}
		}

		if (isNew)
			entity = entityClass.newInstance();

		for (ClassVariable classVariable : classVariables) {
			if (classVariable.getComponent() instanceof CComboBox<?>) {
				classVariable.getSetMethod().invoke(entity,
						((CComboBox<?>) classVariable.getComponent()).getSelectedObject());
			} else {
				switch (classVariable.getClassType().getSimpleName()) {
				case "String":
					classVariable.getSetMethod().invoke(entity,
							((CTextField) classVariable.getComponent()).getText());
					break;
				case "int":
					classVariable.getSetMethod().invoke(entity,
							(int) ((CTextField) classVariable.getComponent()).getDouble());
					break;
				case "double":
					classVariable.getSetMethod().invoke(entity,
							((CTextField) classVariable.getComponent()).getDouble());
					break;
				case "long":
					classVariable.getSetMethod().invoke(entity,
							(long) ((CTextField) classVariable.getComponent()).getDouble());
					break;
				case "Date":
					classVariable.getSetMethod().invoke(entity,
							((CDateChooser) classVariable.getComponent()).getDate());
					break;
				default:
					Utils.showErrorMessage("BEKLENMEYEN GİRDİ HATASI!!!\n"
							+ classVariable.getClassType());
					return;
				}

			}
		}
		if (isNew)
			HibernateManager.getInstance().save(entity);
		else
			HibernateManager.getInstance().update(entity);

		dispose();
	}

	protected void cancelClicked() {
		dispose();
	}

}
