package com.techchallenge.biblioteca.mapper;

import com.techchallenge.biblioteca.dto.UsuarioRequestDTO;
import com.techchallenge.biblioteca.dto.UsuarioResponseDTO;
import com.techchallenge.biblioteca.entity.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }

    public static Usuario toEntity(UsuarioRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        return updateEntity(usuario, requestDTO);
    }

    public static Usuario updateEntity(Usuario usuario, UsuarioRequestDTO requestDTO) {
        if (usuario == null) {
            return null;
        }

        if (requestDTO == null) {
            return usuario;
        }

        usuario.setNome(requestDTO.getNome());
        usuario.setEmail(requestDTO.getEmail());
        return usuario;
    }
}
