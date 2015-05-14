package th.common;

public class Flags {
	private int flags = 0;

	public boolean hasFlags(int flags) {
		return (this.flags & flags) != 0;
	}

	public void setFlags(int flags) {
		this.flags |= flags;
	}

	public void clearFlags(int flags) {
		this.flags &= ~flags;
	}
}
