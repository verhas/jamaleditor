package javax0.jamal.webeditor.security;

import com.vaadin.flow.server.VaadinSession;
import javax0.jamal.webeditor.Configuration;

public class ClientCheck {
    public static void assertIpAllowed() {
        final var clientIp = VaadinSession.getCurrent().getBrowser().getAddress();
        if (!clientIp.equals("127.0.0.1") && !clientIp.equals("0:0:0:0:0:0:0:1")) {
            if (Configuration.INSTANCE.CLIENT != null) {
                if (!Configuration.INSTANCE.CLIENT.matcher(clientIp).matches()) {
                    throw new RuntimeException("Access denied from " + clientIp);
                }
            } else {
                throw new RuntimeException("Access denied from " + clientIp);
            }
        }
    }
}
