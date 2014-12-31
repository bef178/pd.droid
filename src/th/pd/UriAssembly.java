package th.pd;

public class UriAssembly {
	String scheme;
	String host;
	String port;
	String path;

	String getAuthority() {
		return host + ":" + port;
	}

	String getUri() {
		return scheme + "://" + host + ":" + port + "/" + path;
	}
}
