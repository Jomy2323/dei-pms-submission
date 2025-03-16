package pt.ulisboa.tecnico.rnl.dei.dms.exceptions;

public enum ErrorMessage {

    // Person related errors
    NO_SUCH_PERSON("Não existe nenhuma pessoa com o istID %s", 1001),
    PERSON_NAME_NOT_VALID("O nome da pessoa especificado não é válido.", 1002),
    PERSON_ALREADY_EXISTS("Já existe uma pessoa com o ID %s", 1003),
    DUPLICATE_IST_ID("Já existe uma pessoa com o IST ID %s", 1004),
    DUPLICATE_EMAIL("Já existe uma pessoa com o email %s", 1005),
    INVALID_PERSON_TYPE("Tipo de pessoa inválido: %s", 1006),
    ID_NOT_THAT_ROLE("Este istId não possui o estatuto necessário para aceder a este modo de visualização", 1007),
    PERSON_NOT_FOUND("Pessoa com ID %s não encontrada", 1008),
    
    //Workflow

    INVALID_WORKFLOW_TYPE("Workflow só pode ser de tese ou de defesa", 5001),
    // Thesis related errors
    THESIS_NOT_FOUND("Tese com ID %s não encontrada", 2001),
    THESIS_ALREADY_EXISTS("Já existe uma tese para este estudante", 2002),
    INVALID_WORKFLOW_STATE("Estado do workflow de tese inválido: %s", 2003),
    JURY_MEMBER_NOT_FOUND("Membro do júri com ID %s não encontrado", 2004),
    
    // Defense related errors
    DEFENSE_NOT_FOUND("Defesa com ID %s não encontrada", 3001),
    DEFENSE_ALREADY_EXISTS("Já existe uma defesa para esta tese", 3002),
    INVALID_DEFENSE_STATE("Estado do workflow de defesa inválido: %s", 3003),
    
    // Validation errors
    VALIDATION_ERROR("Erro de validação: %s", 4001),
    UNAUTHORIZED("Não autorizado: %s", 4002),
    
    // Generic errors
    INTERNAL_ERROR("Algo correu mal", 9999);

    private final String label;
    private final int code;

    ErrorMessage(String label, int code) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return this.label;
    }

    public int getCode() {
        return this.code;
    }
}