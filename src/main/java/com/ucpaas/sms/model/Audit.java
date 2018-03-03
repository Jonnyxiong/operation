package com.ucpaas.sms.model;

import java.util.List;

import org.joda.time.DateTime;

public class Audit {
	private List<String> sms_audit_lock_list;
	private DateTime begain_lock_time;
	
	public List<String> getSms_audit_lock_list() {
		return sms_audit_lock_list;
	}

	public void setSms_audit_lock_list(List<String> sms_audit_lock_list) {
		this.sms_audit_lock_list = sms_audit_lock_list;
	}
	
	public DateTime getBegain_lock_time() {
		return begain_lock_time;
	}

	public void setBegain_lock_time(DateTime begain_lock_time) {
		this.begain_lock_time = begain_lock_time;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sms_audit_lock_list == null) ? 0 : sms_audit_lock_list.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Audit other = (Audit) obj;
		if (sms_audit_lock_list == null) {
			if (other.sms_audit_lock_list != null)
				return false;
		} else if (!sms_audit_lock_list.equals(other.sms_audit_lock_list))
			return false;
		return true;
	}
	
}
