package animalkeeping.ui;

import animalkeeping.logging.Communicator;
import animalkeeping.model.Person;
import animalkeeping.util.AddDatabaseUserDialog;
import animalkeeping.util.Dialogs;
import animalkeeping.util.EntityHelper;
import animalkeeping.util.SuperUserDialog;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;


import java.sql.Connection;
import java.util.List;

public class PersonsTable extends TableView<Person> {
    private TableColumn<Person, Number> idCol;
    private TableColumn<Person, String> firstNameCol;
    private TableColumn<Person, String> lastNameCol;
    private TableColumn<Person, String> emailCol;
    private ObservableList<Person> masterList = FXCollections.observableArrayList();
    private FilteredList<Person> filteredList;
    private MenuItem newItem, editItem, deleteItem, addToDBItem;

    public PersonsTable() {
        super();
        idCol = new TableColumn<>("id");
        idCol.setCellValueFactory(data -> new ReadOnlyLongWrapper(data.getValue().getId()));
        idCol.prefWidthProperty().bind(this.widthProperty().multiply(0.09));

        firstNameCol = new TableColumn<>("first name");
        firstNameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getFirstName()));
        firstNameCol.prefWidthProperty().bind(this.widthProperty().multiply(0.20));

        lastNameCol = new TableColumn<>("last name");
        lastNameCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLastName()));
        lastNameCol.prefWidthProperty().bind(this.widthProperty().multiply(0.20));

        emailCol= new TableColumn<>("email");
        emailCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getEmail()));
        emailCol.prefWidthProperty().bind(this.widthProperty().multiply(0.50));

        this.getColumns().addAll(idCol, firstNameCol, lastNameCol, emailCol);
        this.setRowFactory( tv -> {
            TableRow<Person> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Person p = row.getItem();
                    p = Dialogs.editPersonDialog(p);
                    if (p != null) {
                        refresh();
                        setSelectedPerson(p);
                    }
                }
            });
            return row ;
        });

        this.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Person>() {
            @Override
            public void onChanged(Change<? extends Person> c) {
                int sel_count = c.getList().size();
                editItem.setDisable(sel_count == 0);
                deleteItem.setDisable(sel_count == 0);
                boolean hasUser = false;
                if (sel_count > 0) {
                    Person p = c.getList().get(0);
                    hasUser = p.getUser() != null;
                }
                addToDBItem.setDisable(sel_count == 0 && hasUser);
            }
        });
        ContextMenu cmenu = new ContextMenu();
        newItem = new MenuItem("new person");
        newItem.setOnAction(event -> editPerson(null));

        editItem = new MenuItem("edit person");
        editItem.setDisable(true);
        editItem.setOnAction(event -> editPerson(this.getSelectionModel().getSelectedItem()));

        deleteItem = new MenuItem("delete person");
        deleteItem.setDisable(true);
        deleteItem.setOnAction(event -> deletePerson(this.getSelectionModel().getSelectedItem()));

        addToDBItem = new MenuItem("add database user to person");
        addToDBItem.setDisable(true);
        addToDBItem.setOnAction(event -> addToDatabase(this.getSelectionModel().getSelectedItem()));
        cmenu.getItems().addAll(newItem, editItem, deleteItem, addToDBItem);

        this.setContextMenu(cmenu);
        //init();
    }

   /* public PersonsTable(ObservableList<Person> items) {
        this();
        this.setItems(items);
    }*/

    private void init() {
        List<Person> result = EntityHelper.getEntityList("from Person", Person.class);
        masterList.addAll(result);
        filteredList = new FilteredList<>(masterList, p -> true);
        SortedList<Person> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(this.comparatorProperty());
        this.setItems(sortedList);
    }

    public void setNameFilter(String name) {
        filteredList.setPredicate(person -> {
            // If filter text is empty, display all persons.
            if (name == null || name.isEmpty()) {
                return true;
            }

            // Compare first name and last name of every person with filter text.
            String lowerCaseFilter = name.toLowerCase();

            if (person.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
                return true; // Filter matches first name.
            } else if (person.getLastName().toLowerCase().contains(lowerCaseFilter)) {
                return true; // Filter matches last name.
            }
            return false; // Does not match.
        });
    }


    public void refresh() {
        masterList.clear();
        masterList.addAll(EntityHelper.getEntityList("from Person", Person.class));
        super.refresh();
    }

    public void setIdFilter(Long id) {
        filteredList.setPredicate(person -> id == null || id.equals(person.getId()));
    }

    public void setSelectedPerson(Person p) {
        this.getSelectionModel().select(p);
    }

    private void editPerson(Person p) {
        Person prsn = Dialogs.editPersonDialog(p);
        if (prsn != null) {
            refresh();
            setSelectedPerson(prsn);
        }
    }

    private void deletePerson(Person p) {
        if (p == null)
            return;
        if (p.getTreatments().size() > 0) {
            Dialogs.showInfo("Cannot delete Person " + p.getLastName() + " since it is referenced by treatments!");
            return;
        }
        Communicator.pushDelete(p);
    }

    private void addToDatabase(Person p) {
        Connection c = SuperUserDialog.openConnection();
        if (c == null) {
            Dialogs.showInfo("Connection refused!");
            return;
        }
        AddDatabaseUserDialog.addDatabaseUser(c, p);
        refresh();
        setSelectedPerson(p);
    }
}
