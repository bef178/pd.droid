package th.pd.mail.tidyface.compose;

import th.common.Flags;

import java.util.LinkedList;
import java.util.List;

public class ComposeModel {

	public static final int FLAG_SHOW_ATTACHMENT_ROW = 1 << 1;
	public static final int FLAG_SHOW_CC_BCC_ROW = 1 << 2;

	private Flags flags = new Flags();
	private String subject;
	private final List<Object> attachmentList = new LinkedList<>();
	private String recipient;
	private String cc;
	private String bcc;
	private String mailContent;

	public List<Object> getAttachments() {
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

	public boolean hasCcOrBcc() {
		return (cc != null && !cc.isEmpty())
				|| (bcc != null && !bcc.isEmpty());
	}

	public boolean isAttachmentRowVisible() {
		return flags.hasFlags(FLAG_SHOW_ATTACHMENT_ROW);
	}

	public boolean isCcBccRowVisible() {
		return flags.hasFlags(FLAG_SHOW_CC_BCC_ROW);
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

	public boolean shouldShowAttachmentRow() {
		return hasAttachment()
				|| flags.hasFlags(FLAG_SHOW_ATTACHMENT_ROW);
	}

	public boolean shouldShowCcBccRow() {
		return hasCcOrBcc() || flags.hasFlags(FLAG_SHOW_CC_BCC_ROW);
	}

	public boolean toggleShowAttachmentRow() {
		if (hasAttachment()) {
			flags.setFlags(FLAG_SHOW_ATTACHMENT_ROW);
			return true;
		}

		if (flags.hasFlags(FLAG_SHOW_ATTACHMENT_ROW)) {
			flags.clearFlags(FLAG_SHOW_ATTACHMENT_ROW);
			return false;
		} else {
			flags.setFlags(FLAG_SHOW_ATTACHMENT_ROW);
			return true;
		}
	}

	public boolean toggleShowCcBccRow() {
		if (hasCcOrBcc()) {
			flags.setFlags(FLAG_SHOW_CC_BCC_ROW);
			return true;
		}

		if (flags.hasFlags(FLAG_SHOW_CC_BCC_ROW)) {
			flags.clearFlags(FLAG_SHOW_CC_BCC_ROW);
			return false;
		} else {
			flags.setFlags(FLAG_SHOW_CC_BCC_ROW);
			return true;
		}
	}
}
