package th.pd.mail.fastsync.network;

public class AuthFailedException extends Exception {

	private static final long serialVersionUID = 5160222390889370997L;

	public AuthFailedException(String msg) {
		super(msg);
	}
}
