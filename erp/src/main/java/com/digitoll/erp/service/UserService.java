package com.digitoll.erp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.digitoll.commons.dto.PasswordUpdateAdminDTO;
import com.digitoll.commons.dto.PasswordUpdateUserDTO;
import com.digitoll.commons.dto.UserDetailsDTO;
import com.digitoll.commons.model.Partner;
import com.digitoll.commons.model.Pos;
import com.digitoll.commons.model.Role;
import com.digitoll.commons.model.User;
import com.digitoll.commons.response.UserDetailsResponse;
import com.digitoll.commons.util.BasicUtils;
import com.digitoll.erp.repository.PartnerRepository;
import com.digitoll.erp.repository.PosRepository;
import com.digitoll.erp.repository.RoleRepository;
import com.digitoll.erp.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private PosRepository posRepository;

    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, NoSuchElementException {

        User user = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        if (!user.isActive()) {
            throw new NoSuchElementException("User is not active");
        }

        List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    }

    public UserDetailsDTO getUserDetailsDto(String username) throws UsernameNotFoundException {

        UserDetailsDTO details = new UserDetailsDTO();

        User userFound = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        BasicUtils.copyPropsSkip(userFound, details, Arrays.asList("password"));

        return details;
    }
    

    public User getUserDetails(String userName) {
        return userRepository.findByUsername(userName);
    }    

    private List<GrantedAuthority> getUserAuthority(List<Role> userRoles) {

        return userRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getCode()))
                .collect(Collectors.toList());
    }

    public UserDetailsDTO createUser(User newUser, String username) {

        User currentUser = userRepository.findByUsername(username);
        boolean isAdmin = roleService.isUserAdmin(currentUser);

        List<Role> roles = new ArrayList<>();

        if (isAdmin) {
            roles = newUser.getRoles().stream()
                    .filter(role -> roleRepository.findById(role.getId()).isPresent())
                    .collect(Collectors.toList());
        } else {

            roleService.getPartnerEmployeeRole().ifPresent(roles::add);
            newUser.setPartnerId(currentUser.getPartnerId());
        }

        // the user should always be new even if an id is sent
        newUser.setId(null);

        newUser.setUsername(newUser.getUsername().toLowerCase());
        newUser.setPassword(BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt()));
        newUser.setRoles(roles);

        User createdUser = userRepository.save(newUser);
        UserDetailsDTO details = new UserDetailsDTO();
        BasicUtils.copyPropsSkip(createdUser, details, Arrays.asList("password"));

        return details;
    }

    public void updatePasswordAdmin(PasswordUpdateAdminDTO updateDto, String username) {

        User currentUser = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        boolean isAdmin = roleService.isUserAdmin(currentUser);
        Optional<User> userToUpdate = userRepository.findById(updateDto.getUserId());

        User user = userToUpdate.orElseThrow(() -> new NoSuchElementException("UserId not found: " + updateDto.getUserId()));

        if (!StringUtils.isEmpty(updateDto.getPassword())) {
            user.setPassword(BCrypt.hashpw(updateDto.getPassword(), BCrypt.gensalt()));
        }

        if (isAdmin
                || currentUser.getPartnerId().equals(user.getPartnerId())) {

            userRepository.save(user);
        } else {
            throw new AccessDeniedException("Not enough permissions to update this user");
        }
    }

    public void updatePasswordUser(PasswordUpdateUserDTO updateDto, String username) {

        User currentUser = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        if (BCrypt.checkpw(updateDto.getOldPassword(), currentUser.getPassword())) {
            if (!StringUtils.isEmpty(updateDto.getNewPassword())) {
                currentUser.setPassword(BCrypt.hashpw(updateDto.getNewPassword(), BCrypt.gensalt()));
                userRepository.save(currentUser);
            }
        }
    }

    public UserDetailsDTO updateUser(UserDetailsDTO updatedUser, String username) throws AccessDeniedException, UsernameNotFoundException {

        User currentUser = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        User user = Optional.ofNullable(userRepository.findByUsername(updatedUser.getUsername().toLowerCase()))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + updatedUser.getUsername()));

        if (!currentUser.getId().equals(user.getId())) {
            throw new AccessDeniedException("Not enough permissions to update this user");
        }

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setPosIds(updatedUser.getPosIds());
        user.setUpdatedAt(new Date());

        User updatedUserResult = userRepository.save(user);

        UserDetailsDTO result = new UserDetailsDTO();
        BasicUtils.copyPropsSkip(updatedUserResult, result, Arrays.asList("password"));

        return result;
    }

    public UserDetailsDTO updateUserAdmin(UserDetailsDTO updatedUser, String username) throws AccessDeniedException, UsernameNotFoundException {

        updatedUser.setUsername(updatedUser.getUsername().toLowerCase());

        User currentUser = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        boolean isAdmin = roleService.isUserAdmin(currentUser);

        User user = Optional.ofNullable(userRepository.findByUsername(updatedUser.getUsername()))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + updatedUser.getUsername()));

        if (isAdmin || currentUser.getPartnerId().equals(user.getPartnerId())) {

            user.setUsername(updatedUser.getUsername());
            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setPosIds(updatedUser.getPosIds());
            user.setUpdatedAt(new Date());

            if (isAdmin) {

                List<Role> roles = updatedUser.getRoles().stream()
                        .filter(role -> roleRepository.findById(role.getId()).isPresent())
                        .collect(Collectors.toList());

                user.setPartnerId(updatedUser.getPartnerId());
                user.setRoles(roles);
            }
        } else {
            throw new AccessDeniedException("Not enough permissions to update this user");
        }

        User updatedUserResult = userRepository.save(user);

        UserDetailsDTO result = new UserDetailsDTO();
        BasicUtils.copyPropsSkip(updatedUserResult, result, Arrays.asList("password"));

        return result;
    }

    public void deleteUser(String userId, String username) throws UsernameNotFoundException, NoSuchElementException {

        User currentUser = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));

        Optional<User> userToDelete = userRepository.findById(userId);

        boolean isAdmin = roleService.isUserAdmin(currentUser);

        User user = userToDelete.orElseThrow(() -> new NoSuchElementException("UserId not found: " + userId));

        if (isAdmin || currentUser.getPartnerId().equals(user.getPartnerId())) {
            userRepository.delete(user);
        } else {
            throw new AccessDeniedException("Not enough permissions to delete this user");
        }
    }

    //user id, because userName might be non unique, since there will be multiple
    // companies
    public UserDetailsResponse getVendorDetails(String userId) throws NoSuchElementException {

        User user = Optional.ofNullable(userRepository.findOneById(userId))
                .orElseThrow(() -> new NoSuchElementException("UserId not found: " + userId));

        Partner partner = Optional.ofNullable(partnerRepository.findOneById(user.getPartnerId()))
                .orElseThrow(() -> new NoSuchElementException("PartnerId not found: " + user.getPartnerId()));

        List<Pos> pos = posRepository.findByPartnerId(partner.getId());

        UserDetailsResponse vendorDetails = new UserDetailsResponse();
        vendorDetails.setPartnerId(partner.getId());
        vendorDetails.setPartnerName(partner.getName());

        //Prevent this being null and modify the "pos" variable above to optional
        if (pos == null || pos.isEmpty()) {
            return vendorDetails;
        }

        vendorDetails.setPosId(pos.get(0).getId());
        vendorDetails.setPosName(pos.get(0).getName());
        return vendorDetails;
    }

    public UserDetailsDTO decommissionUser(String userId, String username) {

        return updateActive(userId, username, false);
    }

    public UserDetailsDTO activateUser(String userId, String username) {

        return updateActive(userId, username, true);
    }

    private UserDetailsDTO updateActive(String userId, String username, boolean active) throws NoSuchElementException, AccessDeniedException {

        User currentUser = userRepository.findByUsername(username);

        User user = Optional.ofNullable(userRepository.findOneById(userId))
                .orElseThrow(() -> new NoSuchElementException("UserId not found: " + userId));

        boolean isAdmin = roleService.isUserAdmin(currentUser);

        if (isAdmin || currentUser.getPartnerId().equals(user.getPartnerId())) {

            user.setActive(active);

            User updatedUserResult = userRepository.save(user);
            UserDetailsDTO result = new UserDetailsDTO();
            BasicUtils.copyPropsSkip(updatedUserResult, result, Arrays.asList("password"));
            return result;

        } else {
            throw new AccessDeniedException("Not enough permissions to change the user's active status");
        }
    }

    public List<UserDetailsDTO> getAllUsers(String username) {

        User currentUser = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new NoSuchElementException("UserId not found: " + username));
        
        boolean isAdmin = roleService.isUserAdmin(currentUser);

        List<User> users;
        
        if (isAdmin) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findAllByPartnerId(currentUser.getPartnerId());
        }
        
        return this.convertUserListToUserDetailsDtoList(users);
    }
    
    private List<UserDetailsDTO> convertUserListToUserDetailsDtoList(List<User> userList) {
        
        Iterator<User> it = userList.iterator();
        List<UserDetailsDTO> resultList = new ArrayList<>();
        
        while(it.hasNext()) {
            UserDetailsDTO userDto = new UserDetailsDTO();
            BasicUtils.copyPropsSkip(it.next(), userDto, Arrays.asList("password"));
            resultList.add(userDto);
        }        
     
        return resultList;
    }

    public List<UserDetailsDTO> getAllUsersByPartnerId(String partnerId) {

        List<User> users = userRepository.findAllByPartnerId(partnerId);
        return this.convertUserListToUserDetailsDtoList(users);
    }

    public User getUserById(String userId, String username) {

        User currentUser = userRepository.findByUsername(username);
        boolean isAdmin = roleService.isUserAdmin(currentUser);

        Optional<User> user;

        if (isAdmin) {

            user = userRepository.findById(userId);
        } else {

            user = userRepository.findByIdAndPartnerId(userId, currentUser.getPartnerId());
        }

        return user.orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
