package pt.ulisboa.tecnico.rnl.dei.dms.person.dto;

public class LoginRequestDto {
    private String istId;
    private String role;

    public LoginRequestDto() {}

    public LoginRequestDto(String istId, String role) {
        this.istId = istId;
        this.role = role;
    }

    public String getIstId() {
        return istId;
    }

    public void setIstId(String istId) {
        this.istId = istId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
