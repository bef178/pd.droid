package th.pd.mail.tidyface.compose;

import android.net.Uri;

import th.common.Flags;
import th.pd.mail.dao.Message;

import java.util.List;

class ComposeModel {

	private static final int FLAG_SHOWS_CC_BCC_ROW = 1 << 2;

	private Flags flags = new Flags(); // the status
	private Message message = new Message();

	public void addAttachment(Uri contentUri) {
		message.attachmentList.add(contentUri);
	}

	public List<Uri> getAttachments() {
		return message.attachmentList;
	}

	public String getBcc() {
		return message.getBlindCarbonCopy();
	}

	public String getCc() {
		return message.getCarbonCopy();
	}

	public String getSender() {
		return message.getSender();
	}

	public String getMailContent() {
		return message.getContent();
	}

	public Message getMessage() {
		return message;
	}

	public String getRecipient() {
		return message.getRecipient();
	}

	public String getSubject() {
		return message.getSubject();
	}

	public boolean hasAttachment() {
		return !message.attachmentList.isEmpty();
	}

	public boolean hasAttachment(Uri contentUri) {
		return message.attachmentList.contains(contentUri);
	}

	public boolean hasCcOrBcc() {
		return (message.getCarbonCopy() != null
				&& !message.getCarbonCopy().isEmpty())
				|| (message.getBlindCarbonCopy() != null
				&& !message.getBlindCarbonCopy().isEmpty());
	}

	public boolean queryStatusIsCcBccRowShown() {
		return flags.hasFlags(FLAG_SHOWS_CC_BCC_ROW);
	}

	public void setBcc(String bcc) {
		this.message.setBlindCarbonCopy(bcc);
	}

	public void setCc(String cc) {
		this.message.setCarbonCopy(cc);
	}

	public void setMailContent(String mailContent) {
		this.message.setContent(mailContent);
	}

	public void setRecipient(String recipient) {
		this.message.setRecipient(recipient);
	}

	public void setSender(String sender) {
		this.message.setSender(sender);
	}

	public void setSubject(String subject) {
		this.message.setSubject(subject);
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
