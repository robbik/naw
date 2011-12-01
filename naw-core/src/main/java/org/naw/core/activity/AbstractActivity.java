package org.naw.core.activity;

import org.naw.core.Process;
import org.naw.core.ProcessContext;

public abstract class AbstractActivity implements Activity {

	protected final String name;

	protected ActivityContext ctx;

	protected ProcessContext procctx;

	protected AbstractActivity(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ActivityContext getActivityContext() {
		return ctx;
	}

	public void init(ActivityContext ctx) throws Exception {
		this.ctx = ctx;
		this.procctx = ctx.getProcessContext();
	}

	public abstract void execute(Process process) throws Exception;

	public void destroy() {
		procctx = null;
		ctx = null;
	}

	@Override
	public String toString() {
		return super.toString() + " [name=" + name + "]";
	}

	@Override
	public int hashCode() {
		return name == null ? 0 : name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (o == this) {
			return true;
		}

		String oname;

		if (o instanceof Activity) {
			oname = ((Activity) o).getName();
		} else if (o instanceof String) {
			oname = (String) o;
		} else {
			return false;
		}

		if (oname == name) {
			return true;
		}

		if ((name == null) || (oname == null)) {
			return false;
		}

		return name.equalsIgnoreCase(oname);
	}
}
