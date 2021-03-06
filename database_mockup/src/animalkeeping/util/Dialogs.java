package animalkeeping.util;

import animalkeeping.logging.Communicator;
import animalkeeping.model.*;
import animalkeeping.ui.Main;
import animalkeeping.ui.forms.*;
import animalkeeping.ui.tables.HousingUnitTable;
import animalkeeping.ui.widgets.HousingDropDown;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.hibernate.Session;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static animalkeeping.util.DateTimeHelper.getDateTime;

public class Dialogs {

    public static void showInfo(String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(info);
        alert.show();
    }


    public static void importSubjectsDialog(HousingUnit unit) {
        AddSubjectsForm htd = new AddSubjectsForm(unit);
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Import subjects");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(htd);
        dialog.setWidth(300);
        htd.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return htd.persistSubjects();
            }
            return null;
        });

        Optional<Boolean> result = dialog.showAndWait();
        if (!result.isPresent() && !result.get()) {
            showInfo("Something went wrong while creating new subjects!");
        } else {
            showInfo("Successfully created new subjects!");
        }
    }


    public static void batchTreatmentDialog(HousingUnit unit) {
        BatchTreatmentForm btf = new BatchTreatmentForm(unit);
        Dialog<List<Treatment>> dialog = new Dialog<>();

        dialog.setTitle("Batch Treatment");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(btf);
        dialog.setWidth(300);
        btf.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return btf.persist();
            }
            return null;
        });

        Optional<List<Treatment>> result = dialog.showAndWait();
        if (result.isPresent()) {
            showInfo("Successfully created a batch treatment!");
        }
    }


    public static HousingType editHousingTypeDialog(HousingType type) {
        HousingTypeForm htd = new HousingTypeForm(type);
        Dialog<HousingType> dialog = new Dialog<>();
        dialog.setTitle("Housing type");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(htd);
        dialog.setWidth(300);
        htd.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return htd.getHousingType();
            }
            return null;
        });
        Optional<HousingType> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                Session session = Main.sessionFactory.openSession();
                session.beginTransaction();
                session.saveOrUpdate(result.get());
                session.getTransaction().commit();
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.get();
        }
        return null;
    }


    public static HousingUnit editHousingUnitDialog(HousingUnit unit) {
        return editHousingUnitDialog(unit, unit != null ? unit.getParentUnit() : null);
    }


    public static HousingUnit editHousingUnitDialog(HousingUnit unit, HousingUnit parent) {
        HousingUnitForm hud = new HousingUnitForm(unit);
        if (unit == null && parent != null) {
            hud.setParentUnit(parent);
        }
        Dialog<HousingUnit> dialog = new Dialog<>();
        dialog.setTitle("Housing unit");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(hud);
        hud.prefWidthProperty().bind(dialog.widthProperty());
        dialog.setWidth(200);
        dialog.setHeight(500);

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return hud.persistHousingUnit();
            }
            return null;
        });
        Optional<HousingUnit> result = dialog.showAndWait();
        return result.orElse(null);
    }


    public static License editLicenseDialog(License l) {
        LicenseForm lf = new LicenseForm(l);
        Dialog<License> dialog = new Dialog<>();
        dialog.setTitle("Add/Edit licence... ");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(lf);
        dialog.setWidth(200);
        lf.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return lf.persistLicense();
            }
            return null;
        });

        Optional<License> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static Quota editQuotaDialog(Quota q) {
        return editQuotaDialog(q,  q.getLicense());
    }

    public static Quota editQuotaDialog(License l) {
        return editQuotaDialog(null, l);
    }

    private static Quota editQuotaDialog(Quota q, License l) {
        QuotaForm qf;
        if (q != null)
            qf = new QuotaForm(q);
        else if (l != null)
            qf = new QuotaForm(l);
        else
            qf = new QuotaForm();
        Dialog<Quota> dialog = new Dialog<>();
        dialog.setTitle("Add/Edit quota ... ");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(qf);
        dialog.setWidth(200);
        qf.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return qf.persistQuota();
            }
            return null;
        });

        Optional<Quota> result = dialog.showAndWait();
        return result.orElse(null);
    }


    public static Pair<Date, Date> getDateInterval() {
        Label startLabel = new Label("Start date:");
        DatePicker sdp = new DatePicker(LocalDate.now().minusYears(1));

        DatePicker edp = new DatePicker(LocalDate.now());
        Label endLabel = new Label("End date:");

        GridPane grid = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        column1.setHgrow(Priority.NEVER);
        ColumnConstraints column2 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(column1, column2);
        edp.prefWidthProperty().bind(column2.maxWidthProperty());
        sdp.prefWidthProperty().bind(column2.maxWidthProperty());

        grid.setVgap(5);
        grid.setHgap(2);
        grid.add(startLabel, 0, 0);
        grid.add(sdp, 1, 0);
        grid.add(endLabel, 0, 1);
        grid.add(edp, 1, 1);

        Dialog<Pair<Date, Date>> dialog = new Dialog<>();
        dialog.setTitle("Specify a time interval ... ");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(grid);
        dialog.setWidth(100);

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                Date start = DateTimeHelper.localDateToUtilDate(sdp.getValue());
                Date end = DateTimeHelper.localDateToUtilDate(edp.getValue());
                if (end.before(start)) {
                    Dialogs.showInfo("End date is before start date. Cancelled!");
                    return null;
                }
                Pair<Date, Date> interval = new Pair<>(start, end);
                return interval;
            }
            return null;
        });

        Optional<Pair<Date, Date>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static Pair<Date, Date> getDateTimeInterval(Date start, Date end) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        LocalDate sd = start != null ? DateTimeHelper.toLocalDate(start) : LocalDate.now().minusYears(1);
        LocalDate ed = end != null ? DateTimeHelper.toLocalDate(end) : LocalDate.now();
        String st = start != null ? timeFormat.format(start) : timeFormat.format(new Date());
        String et = end != null ? timeFormat.format(end) : timeFormat.format(new Date());

        DatePicker sdp = new DatePicker(sd);
        DatePicker edp = new DatePicker(ed);
        TextField sdf = new TextField(st);
        TextField edf = new TextField(et);

        GridPane grid = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        column1.setHgrow(Priority.NEVER);
        ColumnConstraints column2 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(column1, column2);
        edp.prefWidthProperty().bind(column2.maxWidthProperty());
        sdp.prefWidthProperty().bind(column2.maxWidthProperty());
        sdf.prefWidthProperty().bind(column2.prefWidthProperty());
        edf.prefWidthProperty().bind(column2.prefWidthProperty());

        grid.setVgap(5);
        grid.setHgap(2);

        grid.add(new Label("Start date"), 0, 0);
        grid.add(sdp, 1, 0);
        grid.add(new Label("Start time"), 0, 1);
        grid.add(sdf, 1, 1);

        grid.add(new Label("End date"), 0, 2);
        grid.add(edp, 1, 2);
        grid.add(new Label("End time"), 0, 3);
        grid.add(edf, 1, 3);

        Dialog<Pair<Date, Date>> dialog = new Dialog<>();
        dialog.setTitle("Specify a time interval ... ");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(grid);
        dialog.setWidth(100);

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                Date startDate = DateTimeHelper.getDateTime(sdp.getValue(), sdf.getText());
                Date endDate = DateTimeHelper.getDateTime(edp.getValue(), edf.getText());
                if (endDate.before(startDate)) {
                    Dialogs.showInfo("End date is before start date. Cancelled!");
                    return null;
                }
                Pair<Date, Date> interval = new Pair<>(startDate, endDate);
                return interval;
            }
            return null;
        });

        Optional<Pair<Date, Date>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static SubjectType editSubjectTypeDialog(SubjectType type) {
        SubjectTypeForm std = new SubjectTypeForm(type);
        Dialog<SubjectType> dialog = new Dialog<>();
        dialog.setTitle("Subject type");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(std);
        dialog.setWidth(200);
        std.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return std.persistSubjectType();
            }
            return null;
        });
        Optional<SubjectType> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                Session session = Main.sessionFactory.openSession();
                session.beginTransaction();
                session.saveOrUpdate(result.get());
                session.getTransaction().commit();
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return result.get();
        }
        return null;
    }


    public static SpeciesType editSpeciesTypeDialog(SpeciesType type) {
        SpeciesTypeForm std = new SpeciesTypeForm(type);
        Dialog<SpeciesType> dialog = new Dialog<>();
        dialog.setTitle("Species type");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(std);
        dialog.setWidth(200);
        std.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return std.persistSpeciesType();
            }
            return null;
        });
        Optional<SpeciesType> result = dialog.showAndWait();
        return result.orElse(null);
    }


    public static SupplierType editSupplierTypeDialog(SupplierType type) {
        SupplierTypeForm std = new SupplierTypeForm(type);
        Dialog<SupplierType> dialog = new Dialog<>();
        dialog.setTitle("Supplier type");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(std);
        dialog.setWidth(200);
        std.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return std.persistSupplierType();
            }
            return null;
        });
        Optional<SupplierType> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static Person editPersonDialog(Person person) {
        PersonForm pf = new PersonForm(person);
        Dialog<Person> dialog = new Dialog<>();
        dialog.setTitle("Create/edit person...");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(pf);
        dialog.setWidth(300);
        pf.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return pf.persistPerson();
            }
            return null;
        });
        Optional<Person> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static TreatmentType editTreatmentTypeDialog(TreatmentType type) {
        TreatmentTypeForm ttf = new TreatmentTypeForm(type);
        Dialog<TreatmentType> dialog = new Dialog<>();
        dialog.setTitle("Create/edit treatment type...");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(ttf);
        dialog.setWidth(300);
        ttf.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return ttf.persistType();
            }
            return null;
        });
        Optional<TreatmentType> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static Treatment editTreatmentDialog() {
        TreatmentForm tf = new TreatmentForm();
        return editTreatmentDialog(tf);
    }

    public static Treatment editTreatmentDialog(Treatment treatment) {
        TreatmentForm tf = new TreatmentForm(treatment);
        return editTreatmentDialog(tf);
    }


    public static Treatment editTreatmentDialog(Subject subject) {
        TreatmentForm tf = new TreatmentForm(subject);
        return editTreatmentDialog(tf);
    }


    public static Treatment editTreatmentDialog(TreatmentType type) {
        TreatmentForm tf = new TreatmentForm(type);
        return editTreatmentDialog(tf);
    }


    public static Treatment editTreatmentDialog(TreatmentForm form) {
        Dialog<Treatment> dialog = new Dialog<>();
        dialog.setTitle("Create/edit treatment ...");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(form);
        dialog.setWidth(300);
        form.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return form.persistTreatment();
            }
            return null;
        });
        Optional<Treatment> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static Subject editSubjectDialog(Subject s) {
        SubjectForm sf = new SubjectForm(s);

        Dialog<Subject> dialog = new Dialog<>();
        dialog.setTitle("Add/edit subject ...");
        dialog.setHeight(200);
        dialog.setWidth(400);
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(sf);
        sf.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return sf.persistSubject();
            }
            return null;
        });
        Optional<Subject> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static SubjectNote editSubjectNoteDialog(SubjectNote note, Subject subject) {
        SubjectNotesForm snf = new SubjectNotesForm(note, subject);
        return editSubjectNoteDialog(snf);
    }

    public static SubjectNote editSubjectNoteDialog(Subject subject) {
        SubjectNotesForm snf = new SubjectNotesForm(subject);
        return editSubjectNoteDialog(snf);
    }

    public static SubjectNote editSubjectNoteDialog(SubjectNotesForm snf) {
        if (snf == null) {
            return null;
        }
        Dialog<SubjectNote> dialog = new Dialog<>();
        dialog.setTitle("Add/edit note ...");
        dialog.setHeight(300);
        dialog.setWidth(400);
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(snf);

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter(new Callback<ButtonType, SubjectNote>() {
            @Override
            public SubjectNote call(ButtonType b) {
                if (b == buttonTypeOk) {
                    return snf.persist();
                }
                return null;
            }
        });
        Optional<SubjectNote> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static  Housing editHousing(Housing housing) {
        HousingForm form = new HousingForm(housing);

        Dialog<Housing> dialog = new Dialog<>();
        dialog.setTitle("Create/edit housing ...");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(form);
        dialog.setWidth(300);
        form.prefWidthProperty().bind(dialog.widthProperty());

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return form.persistHousing();
            }
            return null;
        });
        Optional<Housing> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public static Subject reportSubjectDead(Subject s) {
        Housing current_housing = s.getCurrentHousing();
        Dialog<Date> dialog = new Dialog<>();
        dialog.setTitle("Report subject dead ...");
        dialog.setHeight(200);
        dialog.setWidth(300);
        dialog.setResizable(true);

        DatePicker dp = new DatePicker();
        dp.setValue(LocalDate.now());
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        TextField timeField = new TextField(timeFormat.format(new Date()));
        ComboBox<Person> personComboBox = new ComboBox<>();
        personComboBox.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person object) {
                return object.getFirstName() + ", " + object.getLastName();
            }

            @Override
            public Person fromString(String string) {
                return null;
            }
        });
        List<Person> persons = EntityHelper.getEntityList("from Person", Person.class);
        personComboBox.getItems().addAll(persons);
        TextArea commentArea = new TextArea();

        GridPane grid = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints(100,100, Double.MAX_VALUE);
        column1.setHgrow(Priority.NEVER);
        ColumnConstraints column2 = new ColumnConstraints(100, 150, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(column1, column2);
        dp.prefWidthProperty().bind(column2.maxWidthProperty());
        timeField.prefWidthProperty().bind(column2.maxWidthProperty());
        personComboBox.prefWidthProperty().bind(column2.maxWidthProperty());
        commentArea.prefWidthProperty().bind(column2.maxWidthProperty());

        grid.add(new Label("subject: "), 0, 0);
        grid.add(new Label( s.getName()), 1, 0);

        grid.add(new Label("date:"), 0, 1);
        grid.add(dp, 1, 1);

        grid.add(new Label("time:"), 0, 2);
        grid.add(timeField, 1, 2);

        grid.add(new Label("person:"), 0, 3);
        grid.add(personComboBox, 1, 3);

        grid.add(new Label("comment:"), 0, 4);
        grid.add(commentArea, 0, 5, 2, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return getDateTime(dp.getValue(), timeField.getText());
            }
            return null;
        });
        Optional<Date> result = dialog.showAndWait();
        if (result.isPresent() && result.get().after(current_housing.getStart())) {
            current_housing.setEnd(result.get());
            SubjectNote note = new SubjectNote("reported dead", commentArea.getText(), result.get(), s);
            note.setPerson(personComboBox.getValue());
            Communicator.pushSaveOrUpdate(note);
            Communicator.pushSaveOrUpdate(current_housing);
            return s;
        }
        return null;
    }

    public static Subject relocateSubjectDialog(Subject s) {
        HousingUnit current_hu = s.getCurrentHousing().getHousing();

        Dialog<HousingUnit> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.setTitle("Select a housing unit");
        dialog.setHeight(300);
        dialog.setWidth(800);
        HousingUnitTable hut = new HousingUnitTable();
        hut.refresh();
        DatePicker dp = new DatePicker();
        dp.setValue(DateTimeHelper.toLocalDate(new Date()));
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        TextField timeField = new TextField(timeFormat.format(new Date()));

        GridPane grid = new GridPane();
        ColumnConstraints column1 = new ColumnConstraints(100,100, Double.MAX_VALUE);
        column1.setHgrow(Priority.NEVER);
        ColumnConstraints column2 = new ColumnConstraints(100, 250, Double.MAX_VALUE);
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(column1, column2);
        dp.prefWidthProperty().bind(column2.maxWidthProperty());
        timeField.prefWidthProperty().bind(column2.maxWidthProperty());
        hut.prefWidthProperty().bind(column2.maxWidthProperty());

        grid.setVgap(5);
        grid.setHgap(2);
        grid.add(new Label("relocation date:"), 0, 0);
        grid.add(dp, 1, 0);

        grid.add(new Label("relocation time:"), 0, 1);
        grid.add(timeField, 1, 1, 1, 1);

        grid.add(new Label("housing unit:"), 0, 2);
        grid.add(hut, 0, 3, 2, 5 );

        //this.getChildren().add(new ScrollPane(housingTable));
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return hut.getSelectedUnit();
            }
            return null;
        });
        Optional<HousingUnit> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != current_hu) {
            LocalDate d = dp.getValue();
            Date currentDate = getDateTime(d, timeField.getText());

            HousingUnit new_hu = result.get();
            Housing current_housing = s.getCurrentHousing();
            if (currentDate.before(current_housing.getStart())) {
                showInfo("Error during relocation of subject. Relocation date before start date of current housing!");
                return null;
            }

            Housing new_housing = new Housing();
            new_housing.setStart(currentDate);
            new_housing.setSubject(s);
            new_housing.setHousing(new_hu);
            current_housing.setEnd(currentDate);
            Communicator.pushSaveOrUpdate(current_housing);
            Communicator.pushSaveOrUpdate(new_housing);
            return s;
        }
        return null;
    }

    public static void showAboutDialog( ) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setResizable(false);
        dialog.setTitle("About animalBase");
        About about = new About();
        dialog.getDialogPane().setContent(about);

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.show();
    }

    public static HousingUnit selectHousingUnit() {
        GridPane grid = new GridPane();
        HousingDropDown hdd = new HousingDropDown();

        grid.add(hdd, 0, 0);

        Dialog<HousingUnit> dialog = new Dialog<>();
        dialog.setTitle("Select a housing unit ... ");
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(grid);
        dialog.setWidth(100);

        ButtonType buttonTypeOk = new ButtonType("ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeCancel);
        dialog.setResultConverter(b -> {
            if (b == buttonTypeOk) {
                return hdd.getHousingUnit();
            }
            return null;
        });

        Optional<HousingUnit> result = dialog.showAndWait();
        return result.orElse(null);
    }
}


