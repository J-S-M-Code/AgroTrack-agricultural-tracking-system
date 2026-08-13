package com.agrotrack.infrastructure.adapters.out.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInviteEmail(String toEmail, String token, String farmName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("agrotrack.no.reply@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Invitación a unirte a " + farmName + " en AgroTrack");
        
        String inviteLink = "http://localhost:4200/accept-invite?token=" + token;
        
        message.setText("Hola,\n\nHas sido invitado a unirte a la finca '" + farmName + "' en AgroTrack.\n" +
                "Por favor, haz clic en el siguiente enlace para establecer tu contraseña y aceptar la invitación:\n\n" +
                inviteLink + "\n\n" +
                "El enlace expirará en 24 horas.\n\n" +
                "Si no esperabas este correo, puedes ignorarlo de forma segura.\n\n" +
                "Saludos,\nEl equipo de AgroTrack.");
                
        mailSender.send(message);
    }
}
