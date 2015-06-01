package th.pd.mail.tidyface.compose;

import android.net.Uri;

import th.common.Flags;

import java.util.LinkedList;
import java.util.List;

public class ComposeModel {

	private static final int FLAG_SHOWS_CC_BCC_ROW = 1 << 2;

	private Flags flags = new Flags(); // the status
	private String subject;
	private final LinkedList<Uri> attachmentList = new LinkedList<>();
	private String recipient;
	private String cc;
	private String bcc;
	private String mailContent;

	public void addAttachment(Uri contentUri) {
		attachmentList.add(contentUri);
	}

	public List<Uri> getAttachments() {
		return attachmentList;
	}

	public String getBcc() {
		return bcc;
	}

	public String getCc() {
		return cc;
	}

	public String getMailContent() {
		return mailContent;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getSubject() {
		return subject;
	}

	public boolean hasAttachment() {
		return !attachmentList.isEmpty();
	}

	public boolean hasAttachment(Uri contentUri) {
		return attachmentList.contains(contentUri);
	}

	public boolean hasCcOrBcc() {
		return (cc != null && !cc.isEmpty())
				|| (bcc != null && !bcc.isEmpty());
	}

	public boolean queryStatusIsCcBccRowShown() {
		return flags.hasFlags(FLAG_SHOWS_CC_BCC_ROW);
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public void setMailContent(String mailContent) {
		this.mailContent = mailContent;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * return <code>true</code> if it is shown after toggled
	 */
	public boolean toggleShowCcBccRow() {
		if (flags.hasFlags(FLAG_SHOWS_CC_BCC_ROW)) {
			flags.clearFlags(FLAG_SHOWS_CC_BCC_ROW);
			return false;
		} else {
			flags.setFlags(FLAG_SHOWS_CC_BCC_ROW);
			return true;
		}
	}
}
