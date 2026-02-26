import com.javarush.island.component.Application;
import com.javarush.island.island.Island;
import com.javarush.island.view.ConsoleView;
import com.javarush.island.view.View;

/**
 * Точка входа.
 */

public class StartConsole {

    public static void main(String[] args) {
        View view = null;
        try {
            Island island = new Island();
            view = new ConsoleView();
            view.setIsland(island);
            Application application = new Application(view, island);
            application.run();
        } catch (Throwable e) {
            if (view != null) {
                view.showThrowable(e);
            } else {
                e.printStackTrace();
            }
        }
    }
}