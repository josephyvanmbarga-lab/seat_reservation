package com.mbarga.seat_reservation.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Profil de l'utilisateur connecté */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(UserProfileResponse.from(currentUser));
    }

    /** Mise à jour email / téléphone */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {

        if (request.getEmail() != null
                && !request.getEmail().equals(currentUser.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Cette adresse email est déjà utilisée");
        }
        if (request.getEmail() != null)    currentUser.setEmail(request.getEmail());
        if (request.getTelephone() != null) currentUser.setTelephone(request.getTelephone());

        return ResponseEntity.ok(UserProfileResponse.from(userRepository.save(currentUser)));
    }

    /** Changement de mot de passe */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody UserPasswordRequest request,
            @AuthenticationPrincipal User currentUser) {

        if (!passwordEncoder.matches(request.getAncienMotDePasse(), currentUser.getPassword())) {
            throw new BadCredentialsException("Mot de passe actuel incorrect");
        }
        currentUser.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        userRepository.save(currentUser);
        return ResponseEntity.noContent().build();
    }

    /** Liste de tous les utilisateurs — ADMIN uniquement (protégé dans SecurityConfig) */
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAll() {
        return ResponseEntity.ok(
                userRepository.findAll().stream().map(UserProfileResponse::from).toList());
    }

    /** Changer le rôle d'un utilisateur — ADMIN uniquement */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserProfileResponse> changeRole(
            @PathVariable Long id,
            @RequestParam Role role,
            @AuthenticationPrincipal User currentUser) {

        if (role == Role.ADMIN) {
            throw new IllegalStateException("Impossible d'attribuer le rôle ADMIN via cette API");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable : id=" + id));
        user.setRole(role);
        return ResponseEntity.ok(UserProfileResponse.from(userRepository.save(user)));
    }
}
