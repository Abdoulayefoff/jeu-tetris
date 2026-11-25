package piece.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of the Observer pattern "subject" side.
 */
public abstract class AbstractListenableModel implements ListenableModel {
	private final List<ListeningModel> listeners;

	public AbstractListenableModel() {
		super();
		this.listeners = new ArrayList<ListeningModel>();
	}

	@Override
	public void addListening(ListeningModel e) {
		this.listeners.add(e);
	}

	@Override
	public void removeListening(ListeningModel e) {
		this.listeners.remove(e);
	}

	protected void fireChangement() {
		// iterate over a copy: a listener may (un)register itself while being notified
		for (ListeningModel e : new ArrayList<>(listeners)) {
			e.modelUpdated(this);
		}
	}
}
