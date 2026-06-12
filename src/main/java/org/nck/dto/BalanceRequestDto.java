package org.nck.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BalanceRequestDto {
    @NotNull
    @Size(min = 10, max = 10, message = "10명을 선택해주세요")
    private List<Long> PlayerIds;
}
