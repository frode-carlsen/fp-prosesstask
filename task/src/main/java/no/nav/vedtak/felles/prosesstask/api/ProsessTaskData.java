package no.nav.vedtak.felles.prosesstask.api;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.sql.rowset.serial.SerialClob;

import org.slf4j.MDC;

/**
 * Task info describing the task to run, including error handling.
 */
public class ProsessTaskData implements ProsessTaskInfo {
    /**
     * Standard properties - aktoerId.
     */
    public static final String AKTØR_ID = "aktoerId"; // NOSONAR //$NON-NLS-1$
    /**
     * Standard properties - behandlingId.
     */
    public static final String BEHANDLING_ID = "behandlingId"; // NOSONAR //$NON-NLS-1$
    /**
     * Standard properties - fagsakId.
     * 
     * @deprecated foretrekk Saksnummer property i stedet
     */
    @Deprecated(forRemoval = true)
    public static final String FAGSAK_ID = "fagsakId"; // NOSONAR //$NON-NLS-1$

    /**
     * Standard properties - saksnummer.
     */
    public static final String SAKSNUMMER = "saksnummer"; // NOSONAR //$NON-NLS-1$

    /**
     * Standard properties - hendelse som tasken venter på eller har behandlet.
     */
    public static final String HENDELSE_PROPERTY = "hendelse"; // NOSONAR //$NON-NLS-1$
    /**
     * Standard properties - hendelse som tasken venter på eller har behandlet.
     */
    public static final String OPPGAVE_ID = "oppgaveId";

    public static final Pattern VALID_KEY_PATTERN = Pattern.compile("[a-zA-Z0-9_\\.]+$"); //$NON-NLS-1$
    private final Properties props = new Properties();
    private final String taskType;
    private int antallFeiledeForsøk;
    private String gruppe;
    private Long id;
    private LocalDateTime nesteKjøringEtter = LocalDateTime.now();
    private Clob payload;
    private int prioritet = 1;
    private String sekvens;
    private String sisteFeil;
    private String sisteFeilKode;
    private String sisteKjøringServerProsess;
    private LocalDateTime sistKjørt;
    private ProsessTaskStatus status = ProsessTaskStatus.KLAR;

    public ProsessTaskData(String taskType) {
        this.taskType = taskType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProsessTaskData)) {
            return false;
        } else if (obj == this) {
            return true;
        }
        ProsessTaskData other = (ProsessTaskData) obj;
        return Objects.equals(taskType, other.taskType)
            && Objects.equals(props, other.props);

    }

    @Override
    public String getAktørId() {
        return getPropertyValue(AKTØR_ID);
    }

    public void setAktørId(String id) {
        setProperty(AKTØR_ID, id);
    }

    @Override
    public int getAntallFeiledeForsøk() {
        return antallFeiledeForsøk;
    }

    public void setAntallFeiledeForsøk(int antallForsøk) {
        this.antallFeiledeForsøk = antallForsøk;
    }

    @Override
    public String getBehandlingId() {
        return getPropertyValue(BEHANDLING_ID);
    }

    protected void setBehandlingId(String id) {
        setProperty(BEHANDLING_ID, id.toString());
    }

    @SuppressWarnings("removal")
    @Override
    public Long getFagsakId() {
        return getPropertyValue(FAGSAK_ID) != null ? Long.valueOf(getPropertyValue(FAGSAK_ID)) : null;
    }

    @Override
    public String getSaksnummer() {
        return getPropertyValue(SAKSNUMMER);
    }

    public void setFagsakId(Long id) {
        setProperty(FAGSAK_ID, id.toString());
    }

    @Override
    public String getGruppe() {
        return gruppe;
    }

    /**
     * Trengs normalt ikke settes.
     * <p>
     * Hvis settes, bør være globalt unik for tasker som henger sammen (eks. kan settes til
     * behandlingId, fagsakId - da vil alle prosess tasks på disse kjøres sekvensielt), men ikke ellers.
     * <p>
     * En spesiell pattern er å sette null første gang, for deretter få tildelt en gruppe og deretter bruke samme gruppe
     * om igjen på flere tasker.
     */
    public void setGruppe(String gruppe) {
        this.gruppe = gruppe;
    }

    @Override
    public Optional<ProsessTaskHendelse> getHendelse() {
        String hendelse = getPropertyValue(HENDELSE_PROPERTY);
        if (hendelse == null) {
            return Optional.empty();
        } else {
            return Optional.of(ProsessTaskHendelse.valueOf(hendelse));
        }
    }

    private void setHendelse(ProsessTaskHendelse hendelse) {
        setProperty(HENDELSE_PROPERTY, hendelse.name());
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getNesteKjøringEtter() {
        return this.nesteKjøringEtter;
    }

    public void setNesteKjøringEtter(LocalDateTime nesteKjøringEtter) {
        this.nesteKjøringEtter = nesteKjøringEtter;
    }

    public Clob getPayload() {
        return payload;
    }

    public void setPayload(Clob payload) {
        this.payload = payload;
    }

    public void setPayload(String payload) {
        try {
            this.payload = (payload != null ? new SerialClob(payload.toCharArray()) : null);
        } catch (SQLException e) {
            // Catcher exception her da constructoren til SerialClob aldri kaster SQLException
            // per d.d. 15.05.2017 og java version "1.8.0_101"
            //
            // Quote fra "SerialClob(char[])"
            // %%% JMB. Agreed. Add code here to throw a SQLException if no
            // support is available for locatorsUpdateCopy=false
            // Serializing locators is not supported.

            throw new IllegalArgumentException("Kan ikke opprette CLOB for håndtering av payload", e); //$NON-NLS-1$
        }
    }

    @Override
    public String getPayloadAsString() {
        if (payload == null) {
            return null;
        }
        try {
            return payload.getSubString(1, (int) payload.length());
        } catch (SQLException e) {
            throw new IllegalArgumentException("Kan ikke hente ut CLOB for håndtering av payload", e); //$NON-NLS-1$
        }
    }

    @Override
    public int getPriority() {
        return prioritet;
    }

    @Override
    public Properties getProperties() {
        return props;
    }

    public void setProperties(Properties props) {
        this.props.putAll(props);
    }

    @Override
    public String getPropertyValue(String key) {
        return props.getProperty(key);
    }

    @Override
    public String getSekvens() {
        return sekvens;
    }

    /**
     * Trengs normalt ikke settes.
     * <p>
     * Vil defaulte til 1.
     * <p>
     * Kun interessant å sette dersom man oppretter flere tasker samtidig som man ønsker skal kjøres
     * sekvensielt (i økende sekvens) eller parallelt(med samme sekvens nr). Dette kan mixes og matches.
     */
    public void setSekvens(String sekvens) {
        this.sekvens = sekvens;
    }

    @Override
    public String getSisteFeil() {
        return sisteFeil;
    }

    public void setSisteFeil(String sisteFeil) {
        this.sisteFeil = sisteFeil;
    }

    @Override
    public String getSisteFeilKode() {
        return sisteFeilKode;
    }

    public void setSisteFeilKode(String sisteFeilKode) {
        this.sisteFeilKode = sisteFeilKode;
    }

    public String getSisteKjøringServerProsess() {
        return sisteKjøringServerProsess;
    }

    public void setSisteKjøringServerProsess(String sisteKjøringServerProsess) {
        this.sisteKjøringServerProsess = sisteKjøringServerProsess;
    }

    @Override
    public LocalDateTime getSistKjørt() {
        return sistKjørt;
    }

    public void setSistKjørt(LocalDateTime sistKjørt) {
        this.sistKjørt = sistKjørt;
    }

    @Override
    public ProsessTaskStatus getStatus() {
        return status;
    }

    public void setStatus(ProsessTaskStatus status) {
        this.status = status;
    }

    @Override
    public String getTaskType() {
        return taskType;
    }

    public Optional<String> getOppgaveId() {
        String value = getPropertyValue(OPPGAVE_ID);
        if (value == null) {
            return Optional.empty();
        } else {
            return Optional.of(value);
        }
    }

    public void setOppgaveId(String oppgaveId) {
        Objects.requireNonNull(oppgaveId);
        setProperty(OPPGAVE_ID, oppgaveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskType, props);
    }

    public ProsessTaskData medNesteKjøringEtter(LocalDateTime tidsstempel) {
        this.nesteKjøringEtter = tidsstempel;
        return this;
    }

    public ProsessTaskData medSekvens(String sekvens) {
        this.sekvens = sekvens;
        return this;
    }

    /**
     * @deprecated bruk {@link #setBehandling(String, String, String)} i stedet.
     */
    @Deprecated(forRemoval = true)
    public void setBehandling(Long fagsakId, Long behandlingId, String aktørId) {
        Objects.requireNonNull(fagsakId, "fagsakId"); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(aktørId, "aktørId"); // NOSONAR //$NON-NLS-1$

        setFagsakId(fagsakId);
        setBehandlingId(behandlingId.toString());
        setAktørId(aktørId);
    }

    /**
     * Convenience API - Angi (optional) hvilken behandling/sak denne prosesstasken kjøres for.
     * 
     * @param saksnummer offisielt saksnummer
     * @param behandlingId angitt behandlingId definert av fagsystem (kan være Long, UUID, etc)
     * @param aktørId angitt AktørId gyldig i AktørRegisteret.
     */
    public void setBehandling(String saksnummer, String behandlingId, String aktørId) {
        Objects.requireNonNull(saksnummer, "saksnummer"); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(aktørId, "aktørId"); // NOSONAR //$NON-NLS-1$

        setSaksnummer(saksnummer);
        setBehandlingId(behandlingId);
        setAktørId(aktørId);
    }

    public void setSaksnummer(String saksnummer) {
        setProperty(SAKSNUMMER, Objects.requireNonNull(saksnummer, "saksnummer"));
    }

    public void setFagsak(Long fagsakId, String aktørId) {
        Objects.requireNonNull(fagsakId, "fagsakId"); // NOSONAR //$NON-NLS-1$
        Objects.requireNonNull(aktørId, "aktørId"); // NOSONAR //$NON-NLS-1$

        setFagsakId(fagsakId);
        setAktørId(aktørId);
    }

    public void setPrioritet(int prioritet) {
        this.prioritet = prioritet;
    }

    public void setProperty(String key, String value) {
        if (!VALID_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("Invalid key:" + key); //$NON-NLS-1$
        }
        if (value == null) {
            this.props.remove(key);
        } else {
            this.props.setProperty(key, value);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
            + "<id=" + getId() //$NON-NLS-1$
            + ", taskType=" + getTaskType() //$NON-NLS-1$
            + ", props=" + getProperties() //$NON-NLS-1$
            + ", status=" + getStatus() //$NON-NLS-1$
            + ">"; //$NON-NLS-1$
    }

    public void venterPåHendelse(ProsessTaskHendelse hendelse) {
        setHendelse(hendelse);
        setStatus(ProsessTaskStatus.VENTER_SVAR);
    }

    public void setCallId(String callId) {
        setProperty(CallId.CALL_ID, callId);
    }

    public void setCallIdFraEksisterende() {
        setCallId(MDC.get(CallId.CALL_ID));
    }

    String getPropertiesAsString() throws IOException {
        if (props.isEmpty()) {
            return null;
        } else {
            StringWriter sw = new StringWriter(200);
            props.store(sw, null);
            return sw.toString();
        }
    }
}
