package animalkeeping.ui;

import animalkeeping.model.Housing;
import animalkeeping.model.HousingUnit;
import animalkeeping.model.Subject;
import animalkeeping.model.Treatment;
import animalkeeping.ui.controller.TimelineController;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.hibernate.Session;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class FishView extends VBox implements Initializable {
    @FXML private ScrollPane tableScrollPane;
    @FXML private TextField idField;
    @FXML private TextField aliasField;
    @FXML private TextField speciesField;
    @FXML private TextField supplierField;
    @FXML private Label aliveField;
    @FXML private TableView<Treatment> treatmentTable;
    @FXML private Tab housingHistoryTab;
    @FXML private VBox timelineVBox;
    @FXML private RadioButton deadOrAliveRadioBtn;

    private SubjectsTable fishTable;
    private HousingTable housingTable;
    private TimelineController timeline;
    private TableColumn<Treatment, Number> idCol;
    private TableColumn<Treatment, String> typeCol;
    private TableColumn<Treatment, Date> startDateCol;
    private TableColumn<Treatment, Date> endDateCol;
    private TableColumn<Treatment, String> nameCol;
    private TableColumn<Treatment, String> personCol;
    private ControlLabel reportDead;
    private ControlLabel moveSubjectLabel;
    private ControlLabel editSubjectLabel;
    private ControlLabel deleteSubjectLabel;
    private ControlLabel addTreatmentLabel;
    private ControlLabel editTreatmentLabel;
    private ControlLabel deleteTreatmentLabel;
    private VBox controls;


    public FishView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/FishView.fxml"));
        loader.setController(this);
        try {
            this.getChildren().add(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fishTable = new SubjectsTable();
        fishTable.getSelectionModel().getSelectedItems().addListener(new FishTableListChangeListener());
        fishTable.setAliveFilter(true);
        timeline = new TimelineController();

        //personsTable.resize();
        this.tableScrollPane.setContent(fishTable);
        this.timelineVBox.getChildren().add(timeline);
        idField.setEditable(false);
        idField.setText("");
        aliasField.setText("");
        aliasField.setEditable(false);
        speciesField.setEditable(false);
        speciesField.setText("");
        supplierField.setEditable(false);
        supplierField.setText("");
        aliveField.setText("");

        idCol = new TableColumn<>("id");
        idCol.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        idCol.prefWidthProperty().bind(treatmentTable.widthProperty().multiply(0.08));

        typeCol = new TableColumn<>("treatment");
        typeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getType().getName()));
        typeCol.prefWidthProperty().bind(treatmentTable.widthProperty().multiply(0.18));

        startDateCol = new TableColumn<>("start");
        startDateCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getStart()));
        startDateCol.prefWidthProperty().bind(treatmentTable.widthProperty().multiply(0.18));

        endDateCol = new TableColumn<>("end");
        endDateCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEnd()));
        endDateCol.prefWidthProperty().bind(treatmentTable.widthProperty().multiply(0.18));

        nameCol = new TableColumn<>("name");
        nameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getSubject().getName()));
        nameCol.prefWidthProperty().bind(treatmentTable.widthProperty().multiply(0.18));

        personCol = new TableColumn<>("person");
        personCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getPerson().getLastName() +
         ", " + data.getValue().getPerson().getFirstName()));
        personCol.prefWidthProperty().bind(treatmentTable.widthProperty().multiply(0.18));

        treatmentTable.getColumns().clear();
        treatmentTable.getColumns().addAll(idCol, typeCol, startDateCol, endDateCol, nameCol, personCol);
        treatmentTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Treatment>) c -> treatmentSelected(c.getList().size() > 0 ? c.getList().get(0) : null));

        housingTable = new HousingTable();
        housingHistoryTab.setContent(housingTable);

        controls = new VBox();
        ControlLabel newSubjectLabel = new ControlLabel("new subject", false);
        controls.getChildren().add(newSubjectLabel);
        editSubjectLabel = new ControlLabel("edit subject", true);
        controls.getChildren().add(editSubjectLabel);
        deleteSubjectLabel = new ControlLabel("delete subject", true);
        deleteSubjectLabel.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                deleteSubject();
            }
        });
        controls.getChildren().add(deleteSubjectLabel);

        controls.getChildren().add(new Separator(Orientation.HORIZONTAL));
        addTreatmentLabel = new ControlLabel("new treatment", true);
        addTreatmentLabel.setTooltip(new Tooltip("add a treatment entry for the selected subject"));
        controls.getChildren().add(addTreatmentLabel);
        editTreatmentLabel = new ControlLabel("edit treatment", true);
        controls.getChildren().add(editTreatmentLabel);
        deleteTreatmentLabel = new ControlLabel("remove treatment", true);
        deleteTreatmentLabel.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                deleteTreatment();
            }
        });
        controls.getChildren().add(deleteTreatmentLabel);

        controls.getChildren().add(new Separator(Orientation.HORIZONTAL));
        ControlLabel newComment = new ControlLabel("add observation", true);
        controls.getChildren().add(newComment);
        ControlLabel editComment = new ControlLabel("edit observation", true);
        controls.getChildren().add(editComment);
        ControlLabel deleteComment = new ControlLabel("delete observation", true);
        controls.getChildren().add(deleteComment);

        controls.getChildren().add(new Separator(Orientation.HORIZONTAL));
        moveSubjectLabel = new ControlLabel("move subject", true);
        moveSubjectLabel.setTooltip(new Tooltip("relocate subject to a different housing unit"));
        moveSubjectLabel.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY)){
                moveSubject(fishTable.getSelectionModel().getSelectedItem());
            }
        });
        controls.getChildren().add(moveSubjectLabel);

        reportDead = new ControlLabel("report dead", true);
        controls.getChildren().add(reportDead);
    }


    private void subjectSelected(Subject s) {
        if (s != null) {
            idField.setText(s.getId().toString());
            aliasField.setText(s.getName());
            speciesField.setText(s.getSpeciesType().getName());
            supplierField.setText(s.getSupplier().getName());
            aliveField.setText("Possibly");
            treatmentTable.getItems().clear();
            treatmentTable.getItems().addAll(s.getTreatments());
            timeline.setTreatments(s.getTreatments());
            housingTable.setHousings(s.getHousings());
        } else {
            idField.setText("");
            aliasField.setText("");
            supplierField.setText("");
            speciesField.setText("");
            aliveField.setText("");
            treatmentTable.getItems().clear();
            timeline.setTreatments(null);
            housingTable.setHousings(null);
        }
        moveSubjectLabel.setDisable(s == null);
        deleteSubjectLabel.setDisable(s == null);
        editSubjectLabel.setDisable(s == null);
        reportDead.setDisable(s == null);
        addTreatmentLabel.setDisable(s==null);
    }

    private void treatmentSelected(Treatment t) {
        editTreatmentLabel.setDisable(t == null);
        deleteTreatmentLabel.setDisable(t == null);
    }

    public void nameFilter(String name) {
        this.fishTable.setNameFilter(name);
    }


    public void idFilter(Long id) {
        this.fishTable.setIdFilter(id);
    }

    private class FishTableListChangeListener implements ListChangeListener<Subject> {
        @Override
        public void onChanged(Change<? extends Subject> c) {
            if (c.getList().size() > 0) {
                subjectSelected(c.getList().get(0));
            }
        }
    }

    @FXML
    private void showAllOrCurrent() {
        fishTable.setAliveFilter(deadOrAliveRadioBtn.isSelected());
    }

    private void deleteSubject() {
        Subject s = fishTable.getSelectionModel().getSelectedItem();
        if (!s.getTreatments().isEmpty()) {
            showInfo("Cannot delete subject " + s.getName() + " since it is referenced by " +
                    Integer.toString(s.getTreatments().size()) + " treatment entries! Delete them first.");
        } else {
            Session session = Main.sessionFactory.openSession();
            session.beginTransaction();
            session.delete(s);
            session.getTransaction().commit();
            session.close();
        }
        fishTable.getSelectionModel().select(null);
    }


    private void deleteTreatment() {
        Treatment t = treatmentTable.getSelectionModel().getSelectedItem();
        Session session = Main.sessionFactory.openSession();
        session.beginTransaction();
        session.delete(t);
        session.getTransaction().commit();
        session.close();

        treatmentTable.getSelectionModel().select(null);
    }

    public VBox getControls() {
        return controls;
    }

    private void showInfo(String  info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(info);
        alert.show();
    }

    private void moveSubject(Subject s) {
        HousingUnit current_hu = s.getCurrentHousing().getHousing();

        Dialog<HousingUnit> dialog = new Dialog<>();
        dialog.setTitle("Select a housing unit");
        dialog.setHeight(200);
        dialog.setWidth(300);
        dialog.setResizable(true);

        HousingUnitTable hut = new HousingUnitTable();
        dialog.getDialogPane().setContent(hut);
        //TODO add Date
        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter(new Callback<ButtonType, HousingUnit>() {
            @Override
            public HousingUnit call(ButtonType b) {
                if (b == buttonTypeOk) {
                    return hut.getSelectedUnit();
                }
                return null;
            }
        });
        Optional<HousingUnit> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != current_hu) {
            Date currentDate = new Date();
            HousingUnit new_hu = result.get();
            Housing current_housing = s.getCurrentHousing();
            Housing new_housing = new Housing();
            new_housing.setStart(currentDate);
            new_housing.setSubject(s);
            new_housing.setHousing(new_hu);
            current_housing.setEnd(currentDate);
            Session session = Main.sessionFactory.openSession();
            session.beginTransaction();
            session.saveOrUpdate(current_housing);
            session.saveOrUpdate(new_housing);
            session.getTransaction().commit();
        }
    }
}