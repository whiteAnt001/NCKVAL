package org.nck.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlayerRegisterRequestDto {
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String name;

    @NotBlank(message = "태그를 입력해주세요.")
    private String tag;
}
