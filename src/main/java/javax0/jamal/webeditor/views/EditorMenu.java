package javax0.jamal.webeditor.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import de.f0rce.ace.enums.AceTheme;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class EditorMenu extends HorizontalLayout {

    final TextField search;
    final TextField replace;
    final Editor editor;
    final Button findButton;
    final Button replaceButton;
    final Button replaceAllButton;

    public EditorMenu(Editor editor) {
        this.editor = editor;
        button(VaadinIcon.PARAGRAPH, editor.aceSrc::setShowInvisibles, editor.aceSrc::isShowInvisibles, false);
        button(VaadinIcon.LEVEL_LEFT, editor.aceSrc::setWrap, editor.aceSrc::isWrap, false);
        button(VaadinIcon.BAR_CHART_V, editor.aceSrc::setShowGutter, editor.aceSrc::isShowGutter, true);

        final var themes = new ComboBox<AceTheme>("Theme");
        themes.setItems(AceTheme.values());
        themes.setItemLabelGenerator(Object::toString);
        themes.addValueChangeListener(e -> editor.aceSrc.setTheme(e.getValue()));
        add(themes);

        search = new TextField();
        search.setPlaceholder("Search");
        replace = new TextField();
        replace.setPlaceholder("Replace");
        add(search, replace);
        search.addValueChangeListener(e -> enableDisable());
        replace.addValueChangeListener(e -> enableDisable());

        findButton = new Button("Find");
        replaceButton = new Button("Replace");
        replaceAllButton = new Button("Replace All");
        findButton.addClickListener(e -> {
            final var start =editor.getCursorPosition(editor.aceSrc.getValue());
            final var text = editor.aceSrc.getValue();
            final var searchFor = search.getValue();
            final var index = text.indexOf(searchFor);
            if (index >= 0) {
                editor.aceSrc.setSelection(index, index + searchFor.length());
            }
        });
        replaceAllButton.addClickListener(e -> {
            final var text = editor.aceSrc.getValue();
            final var newText = text.replace(search.getValue(), replace.getValue());
            editor.aceSrc.setValue(newText);
        });
        replaceButton.addClickListener(e -> {
            final var text = editor.aceSrc.getValue();
            final var newText = text.replaceFirst(Pattern.quote(search.getValue()), replace.getValue());
            editor.aceSrc.setValue(newText);
        });
        final var searchButtons = new HorizontalLayout(findButton, replaceButton, replaceAllButton);
        add(searchButtons);


        enableDisable();
    }

    void enableDisable() {
        search.setEnabled(editor.editedFile != null);
        replace.setEnabled(editor.editedFile != null);
        findButton.setEnabled(editor.editedFile != null && search.getValue().length() > 0);
        replaceButton.setEnabled(editor.editedFile != null && replace.getValue().length() > 0);
        replaceAllButton.setEnabled(editor.editedFile != null && replace.getValue().length() > 0);
    }

    private static final ButtonVariant OFF = ButtonVariant.LUMO_TERTIARY;
    private static final ButtonVariant ON = ButtonVariant.LUMO_PRIMARY;

    private void button(final VaadinIcon vaadinIcon, final Consumer<Boolean> setter, final Supplier<Boolean> getter, boolean deefault) {
        Button button = new Button(vaadinIcon.create());
        button.addClickListener(e -> setter.accept(!getter.get()));
        setter.accept(deefault);
        final var onOff = new Object() {
            boolean sw = deefault;
        };
        button.addClickListener(e -> {
            onOff.sw = !onOff.sw;
            button.removeThemeVariants(ON, OFF);
            button.addThemeVariants(onOff.sw ? ON : OFF);
        });
        add(button);
    }
}
