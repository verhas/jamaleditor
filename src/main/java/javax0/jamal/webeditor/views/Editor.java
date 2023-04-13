package javax0.jamal.webeditor.views;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import de.f0rce.ace.events.AceChanged;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import javax0.jamal.webeditor.Configuration;
import javax0.jamal.webeditor.jamalutils.InterruptingInput;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Jamal Editor")
@Route(value = "", layout = MainLayout.class)
public class Editor extends VerticalLayout implements SelectionListener<Grid<File>, File>, ComponentEventListener<AceChanged> {

    final AceEditor aceSrc = new AceEditor();
    private final Html compiledHtml = new Html(divWrap(""));
    private final TextArea compiledText = new TextArea();

    File editedFile = null;

    private String lastValue;
    private final FileNavTree navTree;
    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
    private final Options asciiDocOptions = Options.builder()
            .attributes(Attributes.builder()
                    .attribute(Attributes.SHOW_TITLE, true)
                    .build())
            .build();
    private EditorMenu menu;

    public Editor() {
        menu = new EditorMenu(this);
        add(menu);

        aceSrc.addAceChangedListener(this);
        aceSrc.getStyle().set("resize", "vertical");
        aceSrc.setReadOnly(true);

        aceSrc.setAutoComplete(Configuration.INSTANCE.AUTO_COMPLETE);
        aceSrc.setTheme(Configuration.INSTANCE.THEME);

        compiledText.setWidth("95%");
        compiledHtml.getStyle().set("resize", "vertical");
        compiledText.getStyle().set("resize", "both");


        final var splitEditor = new SplitLayout(aceSrc, new HorizontalLayout(compiledHtml, compiledText));
        splitEditor.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        splitEditor.setSplitterPosition(50);

        navTree = new FileNavTree(new File("."), this);

        final var split = new SplitLayout(navTree, splitEditor);
        split.setSplitterPosition(15);
        split.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        split.setMinWidth("100%");
        split.setMaxHeight("1000px");
        split.setMinHeight("1000px");
        add(split);

        setMargin(true);
        setHorizontalComponentAlignment(Alignment.END, split);
    }


    @Override
    public void selectionChange(final SelectionEvent<Grid<File>, File> event) {
        editedFile = event.getFirstSelectedItem()
                .filter(File::isFile)
                .orElse(null);
        menu.enableDisable();
        if (editedFile == null) {
            aceSrc.setValue("");
            lastValue = null;
            aceSrc.setReadOnly(true);
            compiledHtml.setHtmlContent(divWrap(""));
            compiledText.setValue("");
        } else {
            try {
                aceSrc.setValue(Files.readString(editedFile.toPath()));
                setEditorMode(editedFile);
                lastValue = aceSrc.getValue();
                aceSrc.setReadOnly(false);
            } catch (IOException e) {
                aceSrc.setValue(e.getMessage());
                aceSrc.setReadOnly(true);
                lastValue = null;
            }
        }
    }

    private static final Map<String, AceMode> MODES = new HashMap<>();

    static {
        MODES.putAll(Map.of("adoc", AceMode.asciidoc,
                "txt", AceMode.text,
                "md", AceMode.markdown
        ));
    }

    private void setEditorMode(final File editedFile) {
        final var fileName = editedFile.getAbsolutePath();
        final String effectiveFileName = fileName.endsWith(".jam") ? fileName.substring(0, fileName.length() - 4) : fileName;
        final int i = effectiveFileName.lastIndexOf('.');
        if (i == -1) {
            aceSrc.setMode(AceMode.text);
            return;
        }
        final var ext = effectiveFileName.substring(i + 1);
        for (final var mode : AceMode.values()) {
            if (mode.name().equalsIgnoreCase(ext)) {
                aceSrc.setMode(mode);
                return;
            }
        }
        if (MODES.containsKey(ext)) {
            aceSrc.setMode(MODES.get(ext));
            return;
        }
        aceSrc.setMode(AceMode.text);
    }


    Position getCursorPosition(final String current) {
        if (lastValue != null) {
            int line = 0;
            int col = 0;
            for (int i = 0; true; i++) {
                if (i >= lastValue.length() || i >= current.length() || lastValue.charAt(i) != current.charAt(i)) {
                    return new Position(null, line, col);
                } else {
                    if (lastValue.length() == current.length()) {
                        return new Position(null, aceSrc.getCursorPosition().getRow(), aceSrc.getCursorPosition().getColumn());
                    }
                    col++;
                    if (lastValue.charAt(i) == '\n') {
                        line++;
                        col = 0;
                    }
                }
            }
        }
        return new Position(null, aceSrc.getCursorPosition().getRow(), aceSrc.getCursorPosition().getColumn());
    }

    @Override
    public void onComponentEvent(final AceChanged event) {
        if (editedFile == null || !event.isFromClient()) return;
        String result;
        try {
            compiledText.setVisible(false);
            compiledHtml.setVisible(true);
            final var file = editedFile.getAbsolutePath();
            saveEditedSourceFile(event, file);
            if (file.endsWith(".jam")) {
                result = handleJamalFile(event, file);
            } else {
                result = event.getValue();
            }
            if (aceSrc.getMode().equals(AceMode.asciidoc)) {
                result = asciidoctor.convert(result, asciiDocOptions);
            } else if (aceSrc.getMode().equals(AceMode.markdown)) {
                result = compileMarkdown(result);
            } else {
                compiledText.setVisible(true);
                compiledHtml.setVisible(false);
            }
        } catch (IOException ex) {
            result = ex.getMessage();
        }
        compiledHtml.setHtmlContent(divWrap(result));
        compiledText.setValue(result);
        lastValue = event.getValue();
    }

    private String handleJamalFile(final AceChanged event, final String file) {
        var jamal = compileJamalFile(event.getValue(), file);
        if (Configuration.INSTANCE.AUTO_COMPLETE) {
            aceSrc.addStaticWordCompleter(calculateCompletions(event.getValue(), getCursorPosition(event.getValue()), file), "macros");
        }
        return jamal;
    }

    private static String compileMarkdown(String input) {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, List.of(new TablesExtension()));

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(input);
        return renderer.render(document);
    }

    private static String divWrap(String input) {
        if (input == null || input.isBlank()) {
            return "<div />";
        }
        return "<div>" + input + "</div>";
    }

    /**
     * Create the list of the word completions that are available at the current cursor position.
     *
     * @param source         the source string to analyse
     * @param cursorPosition the current cursor position
     * @return the list of static word completions
     */
    private List<String> calculateCompletions(final String source, final Position cursorPosition, final String file) {
        javax0.jamal.api.Processor processor = null;
        try (final var p = new Processor("{%", "%}")) {
            processor = p;
            processor.process(new InterruptingInput(cursorPosition, Input.makeInput(source, new Position(file))));
        } catch (Exception ignore) {
        }
        if (processor == null) return List.of();
        final var register = processor.getRegister().debuggable();
        if (register.isEmpty()) {
            return List.of();
        }
        final var scopes = register.get().getScopes();
        final var macroClose = scopes.get(0).getDelimiterPair().close();
        final var words = new ArrayList<String>();
        for (final var scope : scopes) {
            for (final var macro : scope.getUdMacros().keySet()) {
                words.add(macro + macroClose);
            }
            for (final var macro : scope.getMacros().keySet()) {
                if (!words.contains("@" + macro)) {
                    words.add("@" + macro + macroClose);
                    words.add("#" + macro + macroClose);
                }
            }
        }
        return words;
    }

    @NotNull
    private String compileJamalFile(final String source, final String file) {
        final String result;
        try (final var processor = new Processor("{%", "%}")) {
            result = processor.process(Input.makeInput(source, new Position(file)));
            final var outputFile = file.substring(0, file.length() - 4);
            Files.writeString(Paths.get(outputFile), result);
        } catch (Exception e) {
            return e.getMessage();
        }
        return result;
    }

    private static void saveEditedSourceFile(final AceChanged event, final String file) throws IOException {
        Files.write(Paths.get(file), event.getValue().getBytes());
    }
}
