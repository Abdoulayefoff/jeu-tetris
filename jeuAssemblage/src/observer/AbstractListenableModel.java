package observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base implementation of the Observer pattern "subject" side.
 * The listener list is thread-safe because the AI notifies from a worker thread.
 */
public abstract class AbstractListenableModel implements ListenableModel {
	private final List<ListeningModel> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void addListening(ListeningModel e) {
		this.listeners.add(e);
	}

	@Override
	public void removeListening(ListeningModel e) {
		this.listeners.remove(e);
	}

	protected void fireChangement() {
		for (ListeningModel e : listeners) {
			e.modelUpdated(this);
		}
	}
}
