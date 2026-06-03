package PM;

// Stato della sessione corrente (utente loggato)
public class Session {
    public static String userCounter   = null;
    public static String email         = null;
    public static String nome          = null;
    public static String cognome       = null;
    public static int    logId         = -1;
    public static int    roleId        = -1;
    public static String roleNome      = null;
    public static String roleType      = null;   // "AS" | "AC" | "AR" | "U"
    public static int    committenteId = -1;
    public static String sessionToken  = null;
}
