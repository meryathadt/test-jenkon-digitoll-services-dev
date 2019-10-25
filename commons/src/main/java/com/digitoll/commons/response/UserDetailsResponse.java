package com.digitoll.commons.response;

public class UserDetailsResponse {
    private String partnerId;
    private String posId;
    private String partnerName;
    private String posName;

    public UserDetailsResponse(){

    }

    public String getPosName() {
        return posName;
    }

    public void setPosName(String posName) {
        this.posName = posName;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((partnerId == null) ? 0 : partnerId.hashCode());
		result = prime * result + ((partnerName == null) ? 0 : partnerName.hashCode());
		result = prime * result + ((posId == null) ? 0 : posId.hashCode());
		result = prime * result + ((posName == null) ? 0 : posName.hashCode());
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
		UserDetailsResponse other = (UserDetailsResponse) obj;
		if (partnerId == null) {
			if (other.partnerId != null)
				return false;
		} else if (!partnerId.equals(other.partnerId))
			return false;
		if (partnerName == null) {
			if (other.partnerName != null)
				return false;
		} else if (!partnerName.equals(other.partnerName))
			return false;
		if (posId == null) {
			if (other.posId != null)
				return false;
		} else if (!posId.equals(other.posId))
			return false;
		if (posName == null) {
			if (other.posName != null)
				return false;
		} else if (!posName.equals(other.posName))
			return false;
		return true;
	}
     
}
