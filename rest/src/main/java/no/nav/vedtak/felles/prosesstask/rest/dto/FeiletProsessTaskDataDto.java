package no.nav.vedtak.felles.prosesstask.rest.dto;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import io.swagger.v3.oas.annotations.media.Schema;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;

@Schema
public class FeiletProsessTaskDataDto implements ProsessTaskDataInfo {

    private String sisteKjøringServerProsess;
    private String sisteFeilTekst;
    private Integer feiledeForsøk;

    @JsonUnwrapped
    private ProsessTaskDataDto prosessTaskDataDto;

    public FeiletProsessTaskDataDto() {
    }

    public String getSisteKjøringServerProsess() {
        return sisteKjøringServerProsess;
    }

    public void setSisteKjøringServerProsess(String sisteKjøringServerProsess) {
        this.sisteKjøringServerProsess = sisteKjøringServerProsess;
    }

    public String getSisteFeilTekst() {
        return sisteFeilTekst;
    }

    public void setSisteFeilTekst(String sisteFeilTekst) {
        this.sisteFeilTekst = sisteFeilTekst;
    }

    public Integer getFeiledeForsøk() {
        return feiledeForsøk;
    }

    public void setFeiledeForsøk(Integer feiledeForsøk) {
        this.feiledeForsøk = feiledeForsøk;
    }

    public ProsessTaskDataDto getProsessTaskDataDto() {
        return prosessTaskDataDto;
    }

    public void setProsessTaskDataDto(ProsessTaskDataDto prosessTaskDataDto) {
        this.prosessTaskDataDto = prosessTaskDataDto;
    }

    @Override
    public Optional<Sporingsdata> lagSporingsloggData(String action) {
        return prosessTaskDataDto.lagSporingsloggData(action);
    }
}
