package com.tqb.project.model.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
	
	String currentPassword;
	String newPassword;
	String newPasswordRepeat;

}
