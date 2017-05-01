package animalkeeping.ui.controller;

import animalkeeping.model.SpeciesType;
import animalkeeping.model.SubjectType;
import animalkeeping.model.SupplierType;
import animalkeeping.ui.*;
import animalkeeping.util.Dialogs;
import animalkeeping.util.EntityHelper;
// import com.apple.eawt.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;


public class MainViewController extends VBox implements Initializable{
    @FXML private TitledPane animalHousingPane;
    @FXML private TitledPane personsPane;
    @FXML private TitledPane licensesPane;
    @FXML private TitledPane inventoryPane;
    @FXML private TitledPane subjectsPane;
    @FXML private TitledPane treatmentsPane;
    @FXML private TextField idField;
    @FXML private ScrollPane scrollPane;
    @FXML private BorderPane borderPane;
    @FXML private ProgressBar progressBar;
    @FXML private Label messageLabel;
    @FXML private ComboBox<String> findBox;
    @FXML private TitledPane findPane;
    @FXML private Menu speciesTypeMenu;
    @FXML private Menu subjectTypeMenu;
    @FXML private Menu supplierMenu;
    @FXML private MenuItem refreshItem;
    @FXML private HBox hBox;
    @FXML private MenuItem quitMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuBar menuBar;
    @FXML private VBox navigationBar;

    private HashMap<String, TitledPane> panes;
    private HashMap<String, AbstractView> views;


    public MainViewController() {
        URL url = Main.class.getResource("/animalkeeping/ui/fxml/MainView.fxml");
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(url);
        loader.setController(this);
        try {
            this.getChildren().add(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menuBar.setUseSystemMenuBar(true);

        if (System.getProperty("os.name").startsWith("Mac OS X")) {
            System.setProperty("bendalab.animalkeeping", "Animal Keeping");
            //Application.getApplication().setQuitHandler((quitEvent, quitResponse) -> closeApplication());
            //Application.getApplication().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
            //Application.getApplication().setAboutHandler(new myAboutHandler());
            aboutMenuItem.setVisible(false);
            quitMenuItem.setVisible(false);
        }
        findBox.getItems().clear();
        findBox.getItems().addAll("Person", "Subject", "Housing unit", "Treatment");
        findBox.getSelectionModel().select("Subject");
        this.scrollPane.setContent(null);
        if (!Main.isConnected()) {
            LoginController login = new LoginController();
            login.addEventHandler(LoginController.DatabaseEvent.CONNECTING, this::connectedToDatabase);
            login.addEventHandler(LoginController.DatabaseEvent.CONNECTED, this::connectedToDatabase);
            login.addEventHandler(LoginController.DatabaseEvent.FAILED, this::connectedToDatabase);
            this.scrollPane.setContent(login);
        }
        else {
            try{
                connectedToDatabase(null);}
            catch(Exception e){
                e.printStackTrace();
            }
        }
        borderPane.prefHeightProperty().bind(this.prefHeightProperty());
        navigationBar.prefHeightProperty().bind(this.prefHeightProperty());
        inventoryPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            showView("inventory", newValue);
        });
        personsPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            showView("person", newValue);
        });
        animalHousingPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            showView("housing", newValue);
        });
        licensesPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            showView("license", newValue);
        });
        treatmentsPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            showView("treatment", newValue);
        });
        subjectsPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
                showView("subject", newValue);
        });

        panes = new HashMap<>(6);
        panes.put("inventory", inventoryPane);
        panes.put("subject", subjectsPane);
        panes.put("treatment", treatmentsPane);
        panes.put("person", personsPane);
        panes.put("housing", animalHousingPane);
        panes.put("license", licensesPane);
        refreshItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        views = new HashMap<>();
    }


    private Boolean viewIsCached(String name) {
        return views.containsKey(name);
    }

    private void cacheView(String name, AbstractView view) {
        if (!views.containsKey(name)) {
            views.put(name, view);
        }
    }

    private void showView(String type, boolean expanded) {
        if (!type.equals("inventory") && !expanded) {
            inventoryPane.setExpanded(true);
            return;
        }
        if (expanded) {
            this.scrollPane.setContent(null);
            AbstractView view;
            if (viewIsCached(type)) {
                view = views.get(type);
            } else {
                view = createView(type);
            }
            if (view != null) {
                collapsePanes(panes.get(type));
                this.scrollPane.setContent(view);
                refreshView();
                view.requestFocus();
            }
        }
    }

    private AbstractView createView(String type) {
        AbstractView view = null;
        TitledPane pane = null;
        switch (type) {
            case "inventory":
                view = new InventoryController();
                break;
            case "person":
                view = new PersonsView();
                break;
            case "subject":
                view = new SubjectView();
                break;
            case "treatment":
                view = new TreatmentsView();
                break;
            case "license":
                view = new LicenseView();
                break;
            case "housing":
                view = new HousingView();
                break;
            default:
                setIdle("invalid view requested!", true);
        }
        if (view != null) {
            cacheView(type, view);
            view.prefHeightProperty().bind(this.borderPane.heightProperty());
            view.prefWidthProperty().bind(this.borderPane.widthProperty());
            view.addEventHandler(EventType.ROOT, this::handleViewEvents);
            panes.get(type).setContent(view.getControls());
        }
        return view;
    }

    private void handleViewEvents(Event event) {
        if (event.getEventType() == ViewEvent.REFRESHING) {
            setBusy("Refreshing view ...");
        } else if (event.getEventType() == ViewEvent.REFRESHED) {
            setIdle(" ", false);
        } else if (event.getEventType() == ViewEvent.REFRESH_FAIL) {
            setIdle("refreshing failed!", true);
        }
    }

    private Long looksLikeId(String text) {
        Long aLong = null;
        try {
            aLong = Long.parseLong(text);
        } catch (NumberFormatException e) {}
        if (aLong!= null && aLong < 0)
            aLong = null;
        return aLong;
    }

    @FXML
    private void goToId(){
        this.scrollPane.setContent(null);
        Long id = looksLikeId(idField.getText());
        String selectedTable = findBox.getSelectionModel().getSelectedItem();
        if (selectedTable == null) {
            selectedTable = findBox.getItems().get(0);
        }
        if (!Main.isConnected()) {
            return;
        }

        switch (selectedTable) {
            case "Subject":
                subjectsPane.setExpanded(true);
                SubjectView fv = (SubjectView) views.get("subjects");
                if (id != null) {
                    fv.idFilter(id);
                } else {
                    fv.nameFilter(idField.getText());
                }
                //this.scrollPane.setContent(fv);
                break;
            case "Person":
                personsPane.setExpanded(true);
                PersonsView pv = (PersonsView) views.get("persons");
                if (id != null) {
                    pv.idFilter(id);
                } else {
                    pv.nameFilter(idField.getText());
                }
                this.scrollPane.setContent(pv);
                break;
            case "Treatment":
                System.out.println("not yet supported");
                break;
            default:
                System.out.println("invalid selection");
                break;
        }
    }

    @FXML
    private  void closeApplication() {
        Main.getPrimaryStage().close();
    }

    /*
    @FXML
    private void disconnectFromDatabase() {
        Main.sessionFactory.close();
    }
    */

    @FXML
    private void newSubjectType() {
        Dialogs.editSubjectTypeDialog(null);
        fillSubjectTypeMenu();
    }

    @FXML
    private void newSpeciesType() {
        Dialogs.editSpeciesTypeDialog(null);
        fillSpeciesTypeMenu();
    }

    @FXML
    private void newSupplierType() {
        Dialogs.editSupplierTypeDialog(null);
        fillSupplierTypeMenu();
    }

    @FXML
    private void refreshView() {
        if (this.scrollPane.getContent() instanceof View) {
            Task<Void> refreshTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(250);
                    ((View) scrollPane.getContent()).refresh();
                    return null;
                }
            };
            refreshTask.setOnScheduled(event -> setBusy("refreshing ..."));
            refreshTask.setOnSucceeded(event -> setIdle("", false));

            new Thread(refreshTask).start();
        }
    }

    private void fillSubjectTypeMenu() {
        subjectTypeMenu.getItems().clear();
        List<SubjectType> subjectTypeList = EntityHelper.getEntityList("From SubjectType", SubjectType.class);
        MenuItem newSubjectItem = new MenuItem("new");
        newSubjectItem.setOnAction(event -> newSubjectType());
        subjectTypeMenu.getItems().add(newSubjectItem);
        subjectTypeMenu.getItems().add(new SeparatorMenuItem());

        for (SubjectType t : subjectTypeList) {
            Menu submenu = new Menu(t.getName());
            subjectTypeMenu.getItems().add(submenu);
            MenuItem editItem = new MenuItem("edit");
            editItem.setUserData(t);
            editItem.setOnAction(event -> editSubjectType((SubjectType) editItem.getUserData()));
            MenuItem deleteItem = new MenuItem("delete");
            deleteItem.setUserData(t);
            deleteItem.setOnAction(event -> deleteSubjectType((SubjectType) editItem.getUserData()));
            submenu.getItems().add(editItem);
            submenu.getItems().add(deleteItem);
        }
    }


    private void fillSpeciesTypeMenu() {
        speciesTypeMenu.getItems().clear();
        List<SpeciesType> speciesTypeList = EntityHelper.getEntityList("From SpeciesType", SpeciesType.class);

        MenuItem newSpeciesItem = new MenuItem("new");
        newSpeciesItem.setOnAction(event -> newSpeciesType());
        speciesTypeMenu.getItems().add(newSpeciesItem);
        speciesTypeMenu.getItems().add(new SeparatorMenuItem());

        for (SpeciesType t : speciesTypeList) {
            Menu submenu = new Menu(t.getName());
            speciesTypeMenu.getItems().add(submenu);
            MenuItem editItem = new MenuItem("edit");
            editItem.setUserData(t);
            editItem.setOnAction(event -> editSpeciesType((SpeciesType) editItem.getUserData()));
            MenuItem deleteItem = new MenuItem("delete");
            deleteItem.setUserData(t);
            deleteItem.setOnAction(event -> deleteSpeciesType((SpeciesType) editItem.getUserData()));
            submenu.getItems().add(editItem);
            submenu.getItems().add(deleteItem);
        }
    }


    private void fillSupplierTypeMenu() {
        supplierMenu.getItems().clear();
        List<SupplierType> supplier = EntityHelper.getEntityList("From SupplierType", SupplierType.class);
        MenuItem newSupplierItem = new MenuItem("new");
        newSupplierItem.setOnAction(event -> newSupplierType());
        supplierMenu.getItems().add(newSupplierItem);
        supplierMenu.getItems().add(new SeparatorMenuItem());

        for (SupplierType t : supplier) {
            Menu submenu = new Menu(t.getName());
            supplierMenu.getItems().add(submenu);
            MenuItem editItem = new MenuItem("edit");
            editItem.setUserData(t);
            editItem.setOnAction(event -> editSupplierType((SupplierType) editItem.getUserData()));
            MenuItem deleteItem = new MenuItem("delete");
            deleteItem.setUserData(t);
            deleteItem.setOnAction(event -> deleteSupplierType((SupplierType) editItem.getUserData()));
            submenu.getItems().add(editItem);
            submenu.getItems().add(deleteItem);
        }
    }


    private void fillMenus() {
        fillSubjectTypeMenu();
        fillSpeciesTypeMenu();
        fillSupplierTypeMenu();
    }


    private void connectedToDatabase(LoginController.DatabaseEvent event) {
        if (event == null)
            return;
        if(event.getEventType() == LoginController.DatabaseEvent.CONNECTED) {
            for (TitledPane p : panes.values())
                p.setDisable(false);
            subjectTypeMenu.setDisable(false);
            speciesTypeMenu.setDisable(false);
            supplierMenu.setDisable(false);
            fillMenus();
            inventoryPane.setExpanded(true);
            setIdle("Successfully connected to database!", false);
        } else if (event.getEventType() == LoginController.DatabaseEvent.CONNECTING) {
            setBusy("Connecting to database...");
        } else if (event.getEventType() == LoginController.DatabaseEvent.FAILED) {
            setIdle("Connection failed! " + event.getMessage(), true);
        }
    }

    private void collapsePanes(TitledPane excludedPane) {
        for (TitledPane p : panes.values()) {
            if (p != excludedPane && p.isExpanded()) {
                p.setExpanded(false);
            }
        }
    }

    private void editSubjectType(SubjectType t) {
        Dialogs.editSubjectTypeDialog(t);
    }

    private void deleteSubjectType(SubjectType t) {
        if (!EntityHelper.deleteEntity(t)) {
            Dialogs.showInfo("Subject type " + t.getName() + " could not be deleted. Probably referenced by other entries.");
            return;
        }
        fillSubjectTypeMenu();
    }

    private void editSpeciesType(SpeciesType t) {
        Dialogs.editSpeciesTypeDialog(t);
    }

    private void deleteSpeciesType(SpeciesType t) {
        if (!EntityHelper.deleteEntity(t)) {
            Dialogs.showInfo("Species " + t.getName() + " could not be deleted. Probably referenced by other entries.");
            return;
        }
        fillSpeciesTypeMenu();
    }


    private void editSupplierType(SupplierType t) {
        Dialogs.editSupplierTypeDialog(t);
    }

    private void deleteSupplierType(SupplierType t) {
        if (!EntityHelper.deleteEntity(t)) {
            Dialogs.showInfo("Supplier " + t.getName() + " could not be deleted. Probably referenced by other entries.");
            return;
        }
        fillSupplierTypeMenu();
    }

    private void showAbout() {
        System.out.println("Show about");
    }

    /*
    class myAboutHandler implements AboutHandler {
        @Override
        public void handleAbout(AppEvent.AboutEvent aboutEvent) {
            showAbout();
        }
    }
    */
    private void setBusy(String message) {
        Platform.runLater(() -> {
            progressBar.setProgress(-1.0);
            messageLabel.setTextFill(Color.BLACK);
            messageLabel.setText(message != null ? message : "");
        });

    }

    private void setIdle(String message, boolean error) {
        Platform.runLater(() -> {
            progressBar.setProgress(0.);
            if (error) {
                messageLabel.setTextFill(Color.RED);
            } else {
                messageLabel.setTextFill(Color.BLACK);
            }
            messageLabel.setText(message != null ? message : "");
        });

    }
}