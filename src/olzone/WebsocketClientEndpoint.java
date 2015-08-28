package olzone;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint
public class WebsocketClientEndpoint {

    Session userSession = null;
    private MessageHandler messageHandler;
    public static Process forwardProcess;
    public static String port;

    public WebsocketClientEndpoint(String port) {
        try {
        	WebsocketClientEndpoint.port = port;
        	forwardProcess = Runtime.getRuntime().exec(String.format("adb forward tcp:%s tcp:%s", port, port));
        	forwardProcess.waitFor();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.setDefaultMaxSessionIdleTimeout(1000000000);
            container.connectToServer(this, new URI(String.format("ws://localhost:%s/", port)));
            
            new Timer().schedule(new TimerTask() { //refresh forward every
                @Override
                public void run() {
					try {
						String port = WebsocketClientEndpoint.port;
						WebsocketClientEndpoint.forwardProcess = Runtime.getRuntime().exec(String.format("adb forward tcp:%s tcp:%s", port, port));
					} catch (IOException e) {
						e.printStackTrace();
					}
                }
            }, 0, 5000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     * @throws URISyntaxException 
     * @throws IOException 
     * @throws DeploymentException 
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) throws DeploymentException, IOException, URISyntaxException {
        System.out.println("closing websocket, reason: " + reason.getReasonPhrase());    
        forwardProcess.destroy();
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    /**
     * register message handler
     *
     * @param message
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param user
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public static interface MessageHandler {

        public void handleMessage(String message);
    }
}
