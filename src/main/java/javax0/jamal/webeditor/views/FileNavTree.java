package javax0.jamal.webeditor.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.selection.SelectionListener;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FileNavTree extends TreeGrid<File> {

    public FileNavTree(File rootDirectory, SelectionListener<Grid<File>, File> listenerConsumer) {
        addHierarchyColumn(File::getName).setHeader("name");
        setItems(getSubdirectories(rootDirectory), this::getSubdirectories);
        addSelectionListener(listenerConsumer);
        setMinHeight("1000px");
    }

    private static final File[] NO_FILES = new File[0];

    public List<File> getSubdirectories(File parent) {
        if (parent == null || parent.isFile()) return List.of();
        return Arrays.stream(Optional.ofNullable(parent.listFiles()).orElse(NO_FILES))
                .sorted(Comparator.comparing(File::getName))
                .toList();
    }

}
