package javax0.jamal.webeditor.views;


import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import javax0.jamal.webeditor.security.ClientCheck;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final H2 viewTitle;

    public MainLayout() {
        ClientCheck.assertIpAllowed();
        final var editor = new Editor();
        setContent(editor);

        viewTitle = new H2("Jamal Editor");
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");


        addToNavbar(true, toggle, viewTitle);
        H1 appName = new H1("Jamal Editor");
        appName.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.Margin.NONE);

        setPrimarySection(Section.NAVBAR);
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
