package UBC.G8.Message;

import java.util.ArrayList;

public class ChangeSettingsMessage extends Message {
		
	private static final long serialVersionUID = 420L;
	private ArrayList<SettingType> settings = new ArrayList<SettingType>();
	private boolean autolock;
	private String newPassword;
	private int newAttempts;
	private boolean alarmEnable;
	
	public ChangeSettingsMessage(String appId, String safeId, MessageType type) {
		super(appId, safeId, type);
	}

	public boolean isAutolock() {
		return autolock;
	}

	public void setAutolock(boolean autolock) {
		settings.add(SettingType.AUTOLOCKCHANGEREQUEST);
		this.autolock = autolock;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		settings.add(SettingType.CHANGEPASSWORDREQUEST);
		this.newPassword = newPassword;
	}

	public int getNewAttempts() {
		return newAttempts;
	}

	public void setNewAttempts(int newAttempts) {
		settings.add(SettingType.PASSWORDATTEMPTSREQUEST);
		this.newAttempts = newAttempts;
	}

	public boolean isAlarmEnable() {
		return alarmEnable;
	}

	public void setAlarmEnable(boolean alarmEnable) {
		settings.add(SettingType.CHANGEALARMREQUEST);
		this.alarmEnable = alarmEnable;
	}
	
	public ArrayList<SettingType> getSettings()
	{
		return this.settings;
	}

	
	
}
