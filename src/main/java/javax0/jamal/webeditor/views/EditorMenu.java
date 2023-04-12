package javax0.jamal.webeditor.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EditorMenu extends HorizontalLayout {

    public EditorMenu(Editor editor) {
        button(VaadinIcon.PARAGRAPH, editor.aceSrc::setShowInvisibles, editor.aceSrc::isShowInvisibles, false);
        button(VaadinIcon.LEVEL_LEFT, editor.aceSrc::setWrap, editor.aceSrc::isWrap, false);
        button(VaadinIcon.BAR_CHART_V, editor.aceSrc::setShowGutter, editor.aceSrc::isShowGutter, true);
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
