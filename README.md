# PM_Collegio
Progetto Universitario

Specifica Tecnica - Sistema di Prenotazione Multi-Tenant (ma per ora mi limito a svillupare solo un committente)

Il sistema gestisce la prenotazione di camere per più strutture Collegi universitari (committenti), 
con utenti autenticati e ruoli distinti (admin sistema, admin committente, receptionist, cliente). 
Gli utenti possono prenotare camere, visualizzare le proprie prenotazioni e fatture, e gestire il cambio password. 
Gli admin gestiscono strutture, utenti e disponibilità camere. Ogni prenotazione genera una fattura (checkin + checkout), con pagamenti finto supportati (carta, PayPal, bonifico, contanti). 
Il sistema garantisce autenticazione "sicura"(controlla userid e password), ripristino password e monitoraggio degli accessi. 
gestione della sessione(simile perche la session esiste solo per le app web), gli admin possono anche consultare lo storico delle prenotazione.
 
 
PM_Collegio è un sistema desktop sviluppato in Java (Swing/JFrame) per la gestione delle prenotazioni di camere in collegi universitari. 
L'applicazione supporta uno scenario multi-committente ma nella versione attuale è ottimizzata per un singolo committente. 
Il database di supporto è MySQL (schema pm_collegiov2).
L'applicazione offre un'interfaccia grafica completa che guida l'operatore attraverso tutte le fasi operative: 
registrazione degli ospiti, assegnazione delle camere, check-in e check-out, generazione e gestione delle fatture, 
e supervisione amministrativa con controllo degli accessi basato su ruolo.

  Architettura del Sistema Stack Tecnologico
Linguaggio	Java SE (JDK compatibile NetBeans IDE)
UI FrameworkJava Swing / JFrame con AbsoluteLayout (org.netbeans.lib.awtextra)
Database	MySQL 8.x, schema pm_collegio
Driver JDBC	com.mysql.cj.jdbc.Driver (MySQL Connector/J)
Build tool	Apache Ant (build.xml generato da NetBeans)
Pattern architetturale	Monolitico piatto: ogni JFrame gestisce UI, logica applicativa e accesso al DB direttamente

  Package e Struttura File
Tutto il codice risiede nel package PM. Ogni JFrame ha il suo file .java affiancato da un file .form NetBeans per la definizione
visuale dell'interfaccia.

