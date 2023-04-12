package javax0.jamal.webeditor.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.selection.SelectionListener;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class FileNavTree extends TreeGrid<File> {

    private final File rootDirectory;

    public FileNavTree(File rootDirectory, SelectionListener<Grid<File>, File> listenerConsumer) {
        this.rootDirectory = rootDirectory;
        addHierarchyColumn(f -> f.getName() + (f.isFile() ? "" : "/")).setHeader("name");
        setItems(getSubdirectories(rootDirectory), this::getSubdirectories);
        addSelectionListener(listenerConsumer);
        setMinHeight("1000px");
        createContextMenu();
    }

    private void createContextMenu() {
        final var menu = addContextMenu();
        menu.addItem("New File", this::createNewFileDialog);
        menu.addItem("New Directory", this::createNewDirectoryDialog);
        menu.addItem("Rename", this::renameDialog);
        menu.addItem("Delete", this::deleteFileDialog);
    }

    private void deleteFileDialog(final GridContextMenu.GridContextMenuItemClickEvent<File> e) {
        final var file = e.getItem();
        if (file.isPresent()) {
            final var dialog = new Dialog();
            dialog.setHeaderTitle("Delete '" + file.get().getName() + "'?");
            Button deleteButton = new Button("Delete", __ -> {
                deleteFile(file.get());
                dialog.close();
                refresh();
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            Button cancelButton = new Button("Cancel", __ -> dialog.close());
            cancelButton.addClickShortcut(Key.ESCAPE);
            cancelButton.addClickListener(__ -> dialog.close());
            dialog.setDraggable(true);
            dialog.getFooter().add(cancelButton);
            dialog.getFooter().add(deleteButton);
            dialog.open();
        }
    }

    public void refresh() {
        setItems(getSubdirectories(rootDirectory), this::getSubdirectories);
    }

    private record FileDialogContext(String header, String fieldLabel, String actionButtonLabel,
                                     BiConsumer<File, String> action, boolean namePrefilled) {
    }

    private void createDialogWithText(final GridContextMenu.GridContextMenuItemClickEvent<File> e, FileDialogContext ctx) {
        final var file = e.getItem();
        if (file.isPresent()) {
            final var dialog = new Dialog();
            dialog.setHeaderTitle(ctx.header);
            final TextField fileName = new TextField(ctx.fieldLabel);
            if (ctx.namePrefilled) {
                fileName.setValue(file.get().getName());
            }
            fileName.setAutofocus(true);
            Button actionButton = new Button(ctx.actionButtonLabel, __ -> {
                ctx.action.accept(file.get(), fileName.getValue());
                dialog.close();
                refresh();
            });
            actionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            actionButton.addClickShortcut(Key.ENTER);
            Button cancelButton = new Button("Cancel", __ -> dialog.close());
            cancelButton.addClickShortcut(Key.ESCAPE);
            cancelButton.addClickListener(__ -> dialog.close());
            dialog.add(fileName);
            dialog.setDraggable(true);
            dialog.getFooter().add(cancelButton);
            dialog.getFooter().add(actionButton);
            dialog.open();
        }
    }

    private void createNewFileDialog(final GridContextMenu.GridContextMenuItemClickEvent<File> e) {
        createDialogWithText(e, new FileDialogContext("New File", "File Name:", "Create New File", FileNavTree::createNewFile,false));
    }

    private void createNewDirectoryDialog(final GridContextMenu.GridContextMenuItemClickEvent<File> e) {
        createDialogWithText(e, new FileDialogContext("New Directory", "Directory Name:", "Create New Directory", FileNavTree::createNewDirectory,false));
    }

    private void renameDialog(final GridContextMenu.GridContextMenuItemClickEvent<File> e) {
        createDialogWithText(e, new FileDialogContext("Rename", "New Name:", "Rename", FileNavTree::rename,true));
    }

    private static void deleteFile(final File file) {
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    private static void rename(final File file, final String name) {
        //noinspection ResultOfMethodCallIgnored
        file.renameTo(new File(file.getParent(), name));
    }

    private static void createNewFile(final File file, final String name) {
        final var parent = file.isDirectory() ? file : file.getParentFile();
        final var newFile = new File(parent, name);
        try {
            //noinspection ResultOfMethodCallIgnored
            newFile.createNewFile();
        } catch (Exception ignore) {

        }
    }

    private static void createNewDirectory(final File file, final String name) {
        final var parent = file.isDirectory() ? file : file.getParentFile();
        final var newFile = new File(parent, name);
        try {
            //noinspection ResultOfMethodCallIgnored
            newFile.mkdirs();
        } catch (Exception ignore) {

        }
    }

    private static final File[] NO_FILES = new File[0];

    public List<File> getSubdirectories(File parent) {
        if (parent == null || parent.isFile()) return List.of();
        return Arrays.stream(Optional.ofNullable(parent.listFiles()).orElse(NO_FILES))
                .sorted(Comparator.comparing(File::getName))
                .toList();
    }

}
