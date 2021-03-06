package no.nav.vedtak.felles.prosesstask.rest.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultatet av asynkron-restart av feilede prosesstasks")
public class ProsessTaskRetryAllResultatDto {

    @Schema(description = "Prosesstasks som restartes")
    private List<Long> prosessTaskIds = new ArrayList<>();


    public ProsessTaskRetryAllResultatDto() { // NOSONAR Input-dto, ingen behov for initialisering
    }

    public List<Long> getProsessTaskIds() {
        if (this.prosessTaskIds == null) {
            prosessTaskIds = new ArrayList<>();
        }
        return prosessTaskIds;
    }

    public void setProsessTaskIds(List<Long> prosessTaskIds) {
        this.prosessTaskIds = prosessTaskIds;
    }

    public void addProsessTaskId(Long prosessTaskId) {
        if (this.prosessTaskIds == null) {
            prosessTaskIds = new ArrayList<>();
        }
        this.prosessTaskIds.add(prosessTaskId);
    }

}
