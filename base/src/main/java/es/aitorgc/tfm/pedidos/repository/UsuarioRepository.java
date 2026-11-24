package es.aitorgc.tfm.pedidos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import es.aitorgc.tfm.pedidos.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);
}
