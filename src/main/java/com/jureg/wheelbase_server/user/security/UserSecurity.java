package com.jureg.wheelbase_server.user.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.jureg.wheelbase_server.follow.repository.FollowRepository;
import com.jureg.wheelbase_server.shared.api.exception.AccessDeniedException;
import com.jureg.wheelbase_server.shared.api.exception.UserNotFoundException;
import com.jureg.wheelbase_server.user.model.AccountType;
import com.jureg.wheelbase_server.user.model.User;
import com.jureg.wheelbase_server.user.repository.UserRepository;

@Component
public class UserSecurity {

	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	
	public UserSecurity(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }
	
	public boolean isUserPublicOrYourselfOrFollowed(UUID requestedUserId) {

		// Fetch requested user
        Optional<User> requestedUser = userRepository.findById(requestedUserId);
        if (!requestedUser.isPresent()) {
        	throw new UserNotFoundException();
        }
        
        // If requested user is public
        if (requestedUser.get().getAccountType().equals(AccountType.PUBLIC)) {
        	return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
        	throw new AccessDeniedException();
        }
        
        // Check principal
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UUID authenticatedUserId)) {
        	throw new AccessDeniedException();
        }

        // If user accesses his own profile
        if (authenticatedUserId.equals(requestedUserId)) {
            return true;
        }
        
        // If user is following the requested user
        if (followRepository.existsByFollowerIdAndFollowedId(authenticatedUserId, requestedUserId)) {
        	return true;
        }
        
        // Else, user is not authorized
        throw new AccessDeniedException();

    }
	
	public boolean isUserYourself(UUID requestedUserId) {

		// Fetch requested user
        Optional<User> requestedUser = userRepository.findById(requestedUserId);
        if (!requestedUser.isPresent()) {
        	throw new UserNotFoundException();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
        	throw new AccessDeniedException();
        }
        
        // Check principal
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UUID authenticatedUserId)) {
        	throw new AccessDeniedException();
        }

        // If user accesses his own profile
        if (authenticatedUserId.equals(requestedUserId)) {
            return true;
        }
        
        // Else, user is not authorized
        throw new AccessDeniedException();
	}
	
}
