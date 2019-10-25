package com.digitoll.erp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
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

@ContextConfiguration(classes = { UserService.class })
@RunWith(SpringRunner.class)
public class UserServiceTest {

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private PartnerRepository partnerRepository;

	@MockBean
	private PosRepository posRepository;

	@MockBean
	private RoleService roleService;

	@Autowired
	private UserService userService;

	private SimpleDateFormat formatter;

	@Before
	public void init() {
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	}

	@Test
	public void testLoadUserByUsernameSuccess() throws ParseException {

		Role mockRole = new Role();

		mockRole.setCode("ROLE_ADMIN");
		mockRole.setCreatedAt(formatter.parse("2015-09-26T09:30:00.000+0000"));
		mockRole.setId("id");
		mockRole.setName("name");

		String mockedUserName = "test@mail.digitoll";
		User mockedUser = new User();
		List<Role> mockedRoles = Arrays.asList(mockRole);

		mockedUser.setUsername(mockedUserName);
		mockedUser.setPassword("1234");
		mockedUser.setRoles(mockedRoles);

		Mockito.when(userRepository.findByUsername(mockedUserName)).thenReturn(mockedUser);

		List<GrantedAuthority> authorities = mockedUser.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getCode())).collect(Collectors.toList());

		UserDetails mockedUserDetails = new org.springframework.security.core.userdetails.User(mockedUser.getUsername(),
				mockedUser.getPassword(), authorities);

		UserDetails response = userService.loadUserByUsername(mockedUserName);

		assertEquals(mockedUserDetails, response);

	}

	@Test
	public void getUserDetailsDtoSuccess() throws ParseException {
		UserDetailsDTO mockUserDetails = new UserDetailsDTO();
		User repoUser = createMockUserAdminRole();

		Mockito.when(userRepository.findByUsername(repoUser.getUsername())).thenReturn(repoUser);

		BasicUtils.copyPropsSkip(repoUser, mockUserDetails, Arrays.asList("password"));

		UserDetailsDTO response = userService.getUserDetailsDto(repoUser.getUsername());

		assertEquals(mockUserDetails, response);

	}

	@Test
	public void getUserDetailsSuccess() throws ParseException {

		User repoUser = createMockUserAdminRole();

		Mockito.when(userRepository.findByUsername(repoUser.getUsername())).thenReturn(repoUser);

		User response = userService.getUserDetails(repoUser.getUsername());

		assertEquals(repoUser, response);
	}

	@Test
	public void testCreateUserIsAdminSuccess() throws ParseException {

		User insertedMockUser = createMockUserAdminRole();

		User mockedCurrentUser = createMockUserNonAdminRole();

		Optional<Role> insertedMockUserRole = Optional.of(insertedMockUser.getRoles().get(0));
		UserDetailsDTO userDetailsMockDTO = new UserDetailsDTO();

		Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(mockedCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(true);
		Mockito.when(roleRepository.findById(insertedMockUserRole.get().getId())).thenReturn(insertedMockUserRole);

		insertedMockUser.setRoles(Arrays.asList(insertedMockUserRole.get()));
		insertedMockUser.setPassword(BCrypt.hashpw(insertedMockUser.getPassword(), BCrypt.gensalt()));

		Mockito.when(userRepository.save(insertedMockUser)).thenReturn(insertedMockUser);

		BasicUtils.copyPropsSkip(insertedMockUser, userDetailsMockDTO, Arrays.asList("password"));

		UserDetailsDTO response = userService.createUser(insertedMockUser, mockedCurrentUser.getUsername());

		// the userId should always be new even if an id is sent
		userDetailsMockDTO.setId(null);

		assertEquals(userDetailsMockDTO, response);
		assertTrue(response.getRoles().containsAll(userDetailsMockDTO.getRoles()));

	}

	@Test
	public void testCreateUserIsNonAdminSuccess() throws ParseException {
		User insertedMockUser = createMockUserNonAdminRole();
		User mockedCurrentUser = createMockUserNonAdminRole();
		mockedCurrentUser.setPartnerId("1266");
		Optional<Role> partnerEmployeeRole = fetchPartnerEmployeeRole();

		List<Role> rolesToAdd = new ArrayList<>();

		rolesToAdd.add(partnerEmployeeRole.get());

		UserDetailsDTO userDetailsMockDTO = new UserDetailsDTO();

		Mockito.when(userRepository.findByUsername(Mockito.anyString())).thenReturn(mockedCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(false);
		Mockito.when(roleService.getPartnerEmployeeRole()).thenReturn(partnerEmployeeRole);

		insertedMockUser.setRoles(rolesToAdd);
		insertedMockUser.setPartnerId(mockedCurrentUser.getPartnerId());
		insertedMockUser.setPassword(BCrypt.hashpw(insertedMockUser.getPassword(), BCrypt.gensalt()));

		Mockito.when(userRepository.save(insertedMockUser)).thenReturn(insertedMockUser);

		BasicUtils.copyPropsSkip(insertedMockUser, userDetailsMockDTO, Arrays.asList("password"));

		UserDetailsDTO response = userService.createUser(insertedMockUser, mockedCurrentUser.getUsername());

		// the userId should always be new even if an id is sent
		userDetailsMockDTO.setId(null);

		assertEquals(userDetailsMockDTO, response);
		assertTrue(response.getRoles().contains(partnerEmployeeRole.get()));
	}
	
	@Test
	public void testUpdatePasswordAdminSuccessIsAdmin() throws ParseException {
		User currentMockUser = createMockUserAdminRole();
		User updatedMockUser = createMockUserNonAdminRole();
		PasswordUpdateAdminDTO updateMockDTO = new PasswordUpdateAdminDTO();
		Optional<User> mockUserToUpdate = Optional.of(updatedMockUser);
		
		updateMockDTO.setUserId("9999");
		updateMockDTO.setPassword("123");
		
		Mockito.when(userRepository.findByUsername(currentMockUser.getUsername())).thenReturn(currentMockUser);
		Mockito.when(roleService.isUserAdmin(currentMockUser)).thenReturn(true);
		Mockito.when(userRepository.findById(updateMockDTO.getUserId())).thenReturn(mockUserToUpdate);
		
		Mockito.when(userRepository.save(updatedMockUser)).thenReturn(updatedMockUser);
		
		userService.updatePasswordAdmin(updateMockDTO, currentMockUser.getUsername());
		
		Mockito.verify(userRepository).save(updatedMockUser);
	}
	
	
	@Test
	public void testUpdatePasswordAdminSuccessHasEqualPartnerIdAndNoPasswordSuccess() throws ParseException {
		User currentMockUser = createMockUserAdminRole();
		User updatedMockUser = createMockUserNonAdminRole();
		PasswordUpdateAdminDTO updateMockDTO = new PasswordUpdateAdminDTO();
		Optional<User> mockUserToUpdate = Optional.of(updatedMockUser);
		
		updateMockDTO.setUserId("9999");
		
		updatedMockUser.setPartnerId(currentMockUser.getPartnerId());
		
		Mockito.when(userRepository.findByUsername(currentMockUser.getUsername())).thenReturn(currentMockUser);
		Mockito.when(roleService.isUserAdmin(currentMockUser)).thenReturn(false);
		Mockito.when(userRepository.findById(updateMockDTO.getUserId())).thenReturn(mockUserToUpdate);
		
		Mockito.when(userRepository.save(updatedMockUser)).thenReturn(updatedMockUser);
		
		userService.updatePasswordAdmin(updateMockDTO, currentMockUser.getUsername());
		
		Mockito.verify(userRepository).save(updatedMockUser);
		
	}
	
	@Test
	public void testUpdatePasswordUserSuccess() throws ParseException {
		User updatedMockUser = createMockUserNonAdminRole();
		PasswordUpdateUserDTO pswUpdateUserMock = new PasswordUpdateUserDTO();
		
		pswUpdateUserMock.setOldPassword("123");
		pswUpdateUserMock.setNewPassword("1234");
		
		
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
		String oldPwdEncoded = encoder.encode(pswUpdateUserMock.getOldPassword());
		
		updatedMockUser.setPassword(oldPwdEncoded);
		
		Mockito.when(userRepository.findByUsername(updatedMockUser.getUsername())).thenReturn(updatedMockUser);
		Mockito.when(userRepository.save(updatedMockUser)).thenReturn(updatedMockUser);
		
		userService.updatePasswordUser(pswUpdateUserMock, updatedMockUser.getUsername());
		
		Mockito.verify(userRepository).save(updatedMockUser);
	}
	
	@Test
	public void testUpdateUserSuccess() throws ParseException {
		
		User mockedUpdatedUser = createMockUserNonAdminRole();
		UserDetailsDTO updatedMockUserDetails = new UserDetailsDTO();

		updatedMockUserDetails.setFirstName("Digi");
		updatedMockUserDetails.setLastName("Tollius");
		updatedMockUserDetails.setUsername(mockedUpdatedUser.getUsername());
		
		Mockito.when(userRepository.findByUsername(mockedUpdatedUser.getUsername())).thenReturn(mockedUpdatedUser);
		Mockito.when(userRepository.findByUsername(updatedMockUserDetails.getUsername())).thenReturn(mockedUpdatedUser);
		Mockito.when(userRepository.save(mockedUpdatedUser)).thenReturn(mockedUpdatedUser);

		UserDetailsDTO result = userService.updateUser(updatedMockUserDetails, mockedUpdatedUser.getUsername());

		assertEquals(updatedMockUserDetails.getUsername(), result.getUsername());
		assertEquals(updatedMockUserDetails.getFirstName(), result.getFirstName());
		assertEquals(updatedMockUserDetails.getLastName(), result.getLastName());
	}
	
	@Test
	public void testUpdateUserAdminIsAdminSuccess() throws ParseException {
		User mockedUpdatedUser = createMockUserAdminRole();
		UserDetailsDTO updatedMockUserDetails = new UserDetailsDTO();
		UserDetailsDTO updatedMockUserDetailsResult = new UserDetailsDTO();
		Optional<Role> partnerEmployeeRole = fetchPartnerEmployeeRole();
		
		String posID1 = "1266";
		String posID2 = "5432";
		
		List<String> posIds = Arrays.asList(posID1, posID2);
		
		updatedMockUserDetails.setFirstName("Digi");
		updatedMockUserDetails.setLastName("Tollius");
		updatedMockUserDetails.setPosIds(posIds);
		updatedMockUserDetails.setUsername(mockedUpdatedUser.getUsername());
		updatedMockUserDetails.setRoles(Arrays.asList(partnerEmployeeRole.get()));
		updatedMockUserDetails.setPartnerId("4655");
		
		Mockito.when(userRepository.findByUsername(mockedUpdatedUser.getUsername())).thenReturn(mockedUpdatedUser);
		Mockito.when(roleService.isUserAdmin(mockedUpdatedUser)).thenReturn(true);
		Mockito.when(userRepository.findByUsername(updatedMockUserDetails.getUsername())).thenReturn(mockedUpdatedUser);
		
		mockedUpdatedUser.setFirstName(updatedMockUserDetails.getFirstName());
		mockedUpdatedUser.setLastName(updatedMockUserDetails.getLastName());
		mockedUpdatedUser.setPosIds(updatedMockUserDetails.getPosIds());
		
		Mockito.when(roleRepository.findById(updatedMockUserDetails.getRoles().get(0).getId())).thenReturn(partnerEmployeeRole);
		
		mockedUpdatedUser.setRoles(Arrays.asList(partnerEmployeeRole.get()));
		mockedUpdatedUser.setPartnerId(updatedMockUserDetails.getPartnerId());
		
		Mockito.when(userRepository.save(mockedUpdatedUser)).thenReturn(mockedUpdatedUser);
		
		BasicUtils.copyPropsSkip(mockedUpdatedUser, updatedMockUserDetailsResult, Arrays.asList("password"));
		
		UserDetailsDTO result = userService.updateUserAdmin(updatedMockUserDetails, mockedUpdatedUser.getUsername());
		
		assertEquals(updatedMockUserDetailsResult, result);
		assertTrue(result.getRoles().contains(partnerEmployeeRole.get()));
		
	}
	
	
	@Test
	public void testUpdateUserAdminIsPartnerIdEqualSuccess() throws ParseException {
		User mockedCurrentUser = createMockUserNonAdminRole2();
		User mockedUpdatedUser = createMockUserNonAdminRole();
		UserDetailsDTO updatedMockUserDetailsResult = new UserDetailsDTO();
		
		mockedUpdatedUser.setPartnerId(mockedCurrentUser.getPartnerId());
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(false);
		Mockito.when(userRepository.findByUsername(mockedUpdatedUser.getUsername())).thenReturn(mockedUpdatedUser);
		Mockito.when(userRepository.save(mockedUpdatedUser)).thenReturn(mockedUpdatedUser);
		
		BasicUtils.copyPropsSkip(mockedUpdatedUser, updatedMockUserDetailsResult, Arrays.asList("password"));
		
		Role nonInsertedRole1 = new Role();
		Role nonInsertedRole2 = new Role();
		
		nonInsertedRole1.setCode("4412");
		nonInsertedRole1.setId("55");
		nonInsertedRole1.setName("test");
		nonInsertedRole1.setCreatedAt(new Date(1992, 06, 10));
		
		nonInsertedRole2.setCode("5566");
		nonInsertedRole1.setId("66");
		nonInsertedRole1.setName("test2");
		nonInsertedRole1.setCreatedAt(new Date(1992, 06, 11));
		
		
		List<Role> mockNonInsertedRoles = Arrays.asList(nonInsertedRole1, nonInsertedRole2);
		
		updatedMockUserDetailsResult.setRoles(mockNonInsertedRoles);
				
		UserDetailsDTO result = userService.updateUserAdmin(updatedMockUserDetailsResult, mockedCurrentUser.getUsername());
		
        assertEquals(2, updatedMockUserDetailsResult.getRoles().size());
        assertEquals(1, result.getRoles().size());
        assertEquals(result.getRoles(), mockedUpdatedUser.getRoles());
		
	}
	
	@Test
	public void testDeleteUserIsAdminSuccess() throws ParseException  {
		User mockedCurrentUser = createMockUserAdminRole();
		Optional<User> deletedMockUser = Optional.of(createMockUserNonAdminRole());
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findById(deletedMockUser.get().getId())).thenReturn(deletedMockUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(true);
		
		userService.deleteUser(deletedMockUser.get().getId(), mockedCurrentUser.getUsername());
		
		Mockito.verify(userRepository, atLeastOnce()).delete(deletedMockUser.get());
	}
	
	@Test
	public void testDeleteUserEqualPartnerIDsSuccess() throws ParseException  {
		User mockedCurrentUser = createMockUserAdminRole();
		Optional<User> deletedMockUser = Optional.of(createMockUserNonAdminRole());
		
		deletedMockUser.get().setPartnerId(mockedCurrentUser.getPartnerId());
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findById(deletedMockUser.get().getId())).thenReturn(deletedMockUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(false);
		
		userService.deleteUser(deletedMockUser.get().getId(), mockedCurrentUser.getUsername());
		
		Mockito.verify(userRepository, atLeastOnce()).delete(deletedMockUser.get());
	}
	
	@Test
	public void testDecommissionUserIsAdminSuccess() throws ParseException {
		User mockedCurrentUser = createMockUserAdminRole();
		Optional<User> mockedDecommissionedUser = Optional.of(createMockUserNonAdminRole());
		UserDetailsDTO decommissionedUserDetailsDTO = new UserDetailsDTO();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findOneById(mockedDecommissionedUser.get().getId())).thenReturn(mockedDecommissionedUser.get());
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(true);
		
		mockedDecommissionedUser.get().setActive(false);
		
		Mockito.when(userRepository.save(mockedDecommissionedUser.get())).thenReturn(mockedDecommissionedUser.get());
		
		BasicUtils.copyPropsSkip(mockedDecommissionedUser.get(), decommissionedUserDetailsDTO, Arrays.asList("password"));
		
		UserDetailsDTO result = userService.decommissionUser(mockedDecommissionedUser.get().getId(), mockedCurrentUser.getUsername());
		
		assertEquals(decommissionedUserDetailsDTO, result);
		assertFalse(result.isActive());
		
	}
	
	@Test
	public void testDecommissionUserEqualPartnerIdSuccess() throws ParseException {
		User mockedCurrentUser = createMockUserNonAdminRole();
		Optional<User> mockedDecommissionedUser = Optional.of(createMockUserNonAdminRole());
		UserDetailsDTO decommissionedUserDetailsDTO = new UserDetailsDTO();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findOneById(mockedDecommissionedUser.get().getId())).thenReturn(mockedDecommissionedUser.get());
		
		
		
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(false);
		
		mockedDecommissionedUser.get().setActive(false);
		mockedDecommissionedUser.get().setPartnerId(mockedCurrentUser.getPartnerId());
		
		
		Mockito.when(userRepository.save(mockedDecommissionedUser.get())).thenReturn(mockedDecommissionedUser.get());
		
		BasicUtils.copyPropsSkip(mockedDecommissionedUser.get(), decommissionedUserDetailsDTO, Arrays.asList("password"));
		
		UserDetailsDTO result = userService.decommissionUser(mockedDecommissionedUser.get().getId(), mockedCurrentUser.getUsername());
		
		assertEquals(decommissionedUserDetailsDTO, result);
		assertFalse(result.isActive());
		
	}
	
	
	@Test
	public void testActivateUserIsAdminSuccess() throws ParseException {
		User mockedCurrentUser = createMockUserAdminRole();
		Optional<User> mockedDecommissionedUser = Optional.of(createMockUserNonAdminRole());
		UserDetailsDTO decommissionedUserDetailsDTO = new UserDetailsDTO();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findOneById(mockedDecommissionedUser.get().getId())).thenReturn(mockedDecommissionedUser.get());
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(true);
		
		mockedDecommissionedUser.get().setActive(true);
		
		Mockito.when(userRepository.save(mockedDecommissionedUser.get())).thenReturn(mockedDecommissionedUser.get());
		
		BasicUtils.copyPropsSkip(mockedDecommissionedUser.get(), decommissionedUserDetailsDTO, Arrays.asList("password"));
		
		UserDetailsDTO result = userService.activateUser(mockedDecommissionedUser.get().getId(), mockedCurrentUser.getUsername());
		
		assertEquals(decommissionedUserDetailsDTO, result);
		assertTrue(result.isActive());
		
	}
	
	@Test
	public void testActivateUserEqualPartnerIdSuccess() throws ParseException {
		User mockedCurrentUser = createMockUserNonAdminRole();
		Optional<User> mockedDecommissionedUser = Optional.of(createMockUserNonAdminRole());
		UserDetailsDTO decommissionedUserDetailsDTO = new UserDetailsDTO();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findOneById(mockedDecommissionedUser.get().getId())).thenReturn(mockedDecommissionedUser.get());
		
		
		
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(false);
		
		mockedDecommissionedUser.get().setActive(true);
		mockedDecommissionedUser.get().setPartnerId(mockedCurrentUser.getPartnerId());
		
		
		Mockito.when(userRepository.save(mockedDecommissionedUser.get())).thenReturn(mockedDecommissionedUser.get());
		
		BasicUtils.copyPropsSkip(mockedDecommissionedUser.get(), decommissionedUserDetailsDTO, Arrays.asList("password"));
		
		UserDetailsDTO result = userService.activateUser(mockedDecommissionedUser.get().getId(), mockedCurrentUser.getUsername());
		
		assertEquals(decommissionedUserDetailsDTO, result);
		assertTrue(result.isActive());
		
	}
	
	@Test
	public void testGetVendorDetailsSuccess() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Partner updatedMockPartner = createMockPartner();
		UserDetailsResponse mockUserDetailsResponse = new UserDetailsResponse();
		
		Pos mockPos = new Pos();
		mockPos.setCode("4412");
		mockPos.setId("12");
		mockPos.setKapschPosId("3612");
		mockPos.setName("TestPOS");
		mockPos.setPartnerId("4812");
		mockPos.setPosIdInPartnersDb("3380");
		
		List<Pos> mockPosList = Arrays.asList(mockPos);
		
		Mockito.when(userRepository.findOneById(mockCurrentUser.getId())).thenReturn(mockCurrentUser);
		Mockito.when(partnerRepository.findOneById(mockCurrentUser.getPartnerId())).thenReturn(updatedMockPartner);
		Mockito.when(posRepository.findByPartnerId(updatedMockPartner.getId())).thenReturn(mockPosList);
		
		mockUserDetailsResponse.setPartnerId(updatedMockPartner.getId());
		mockUserDetailsResponse.setPartnerName(updatedMockPartner.getName());
		mockUserDetailsResponse.setPosId(mockPosList.get(0).getId());
		mockUserDetailsResponse.setPosName(mockPosList.get(0).getName());
		
		UserDetailsResponse response = userService.getVendorDetails(mockCurrentUser.getId());
		
		assertEquals(mockUserDetailsResponse, response);
	}
	
	@Test
	public void testGetVendorDetailsNoPOSSuccess() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Partner updatedMockPartner = createMockPartner();
		UserDetailsResponse mockUserDetailsResponse = new UserDetailsResponse();
		
		List<Pos> mockPosList = new ArrayList<>();
		
		Mockito.when(userRepository.findOneById(mockCurrentUser.getId())).thenReturn(mockCurrentUser);
		Mockito.when(partnerRepository.findOneById(mockCurrentUser.getPartnerId())).thenReturn(updatedMockPartner);
		Mockito.when(posRepository.findByPartnerId(updatedMockPartner.getId())).thenReturn(mockPosList);
		
		mockUserDetailsResponse.setPartnerId(updatedMockPartner.getId());
		mockUserDetailsResponse.setPartnerName(updatedMockPartner.getName());
		
		UserDetailsResponse response = userService.getVendorDetails(mockCurrentUser.getId());
		
		assertEquals(mockUserDetailsResponse, response);
	}
	
	@Test
	public void testGetAllUsersIsAdminSuccess() throws ParseException {
		User currentMockUser = createMockUserAdminRole();
		List<User> mockUsers = Arrays.asList(createMockUserNonAdminRole());
		
		Mockito.when(userRepository.findByUsername(currentMockUser.getUsername())).thenReturn(currentMockUser);
		Mockito.when(roleService.isUserAdmin(currentMockUser)).thenReturn(true);
		Mockito.when(userRepository.findAll()).thenReturn(mockUsers);
		
		
		List<UserDetailsDTO> mockUserDetails = Arrays.asList(new UserDetailsDTO(mockUsers.get(0)));
		
		List<UserDetailsDTO> result = userService.getAllUsers(currentMockUser.getUsername());
		
		assertEquals(mockUserDetails, result);
	}
	
	@Test
	public void testGetAllUsersPartnerIdSuccess() throws ParseException {
		User currentMockUser = createMockUserNonAdminRole();
		List<User> mockUsers = Arrays.asList(createMockUserNonAdminRole());
		
		Mockito.when(userRepository.findByUsername(currentMockUser.getUsername())).thenReturn(currentMockUser);
		Mockito.when(roleService.isUserAdmin(currentMockUser)).thenReturn(false);
		Mockito.when(userRepository.findAllByPartnerId(currentMockUser.getPartnerId())).thenReturn(mockUsers);
		
		List<UserDetailsDTO> mockUserDetails = Arrays.asList(new UserDetailsDTO(mockUsers.get(0)));
		
		List<UserDetailsDTO> result = userService.getAllUsers(currentMockUser.getUsername());
		
		assertEquals(mockUserDetails, result);
	}
	
	@Test
	public void testGetAllUsersByPartnerIdSuccess() throws ParseException {
		String partnerMockId = "1266";
		List<User> mockReturnedRolesByPartnerId = Arrays.asList(createMockUserNonAdminRole());
		
		Mockito.when(userRepository.findAllByPartnerId(partnerMockId)).thenReturn(mockReturnedRolesByPartnerId);
		
		List<UserDetailsDTO> MockUserDetailsRolesByPartnerId = Arrays.asList(new UserDetailsDTO(mockReturnedRolesByPartnerId.get(0)));
		
		List<UserDetailsDTO> response = userService.getAllUsersByPartnerId(partnerMockId);
		
		assertEquals(MockUserDetailsRolesByPartnerId, response);
	}
	
	@Test
	public void testGetUserByIdIsAdminSuccess() throws ParseException {
		User currentMockUser = createMockUserAdminRole();
		User returnedMockUser = createMockUserNonAdminRole();
		
		Mockito.when(userRepository.findByUsername(currentMockUser.getUsername())).thenReturn(currentMockUser);
		Mockito.when(roleService.isUserAdmin(currentMockUser)).thenReturn(true);
		Mockito.when(userRepository.findById(Mockito.anyString())).thenReturn(Optional.of(returnedMockUser));
		
		User response = userService.getUserById(Mockito.anyString(), currentMockUser.getUsername());
		
		assertEquals(returnedMockUser, response);
		
	}
	
	@Test
	public void testGetUserByIdAndPartnerIdSuccess() throws ParseException {
		User currentMockUser = createMockUserNonAdminRole();
		User returnedMockUser = createMockUserNonAdminRole();
		
		Mockito.when(userRepository.findByUsername(currentMockUser.getUsername())).thenReturn(currentMockUser);
		Mockito.when(roleService.isUserAdmin(currentMockUser)).thenReturn(false);
		Mockito.when(userRepository.findByIdAndPartnerId(returnedMockUser.getId(), currentMockUser.getPartnerId())).thenReturn(Optional.of(returnedMockUser));
		
		User response = userService.getUserById(returnedMockUser.getId(), currentMockUser.getUsername());
		
		assertEquals(returnedMockUser, response);
		
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void loadUserByUsernameNoUsernameFail() throws ParseException {
		User mockedUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findByUsername(mockedUser.getUsername())).thenReturn(null);
		
		userService.loadUserByUsername(mockedUser.getUsername());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void loadUserByUsernameIsNotActiveFail() throws ParseException {
		User mockedUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findByUsername(mockedUser.getUsername())).thenReturn(mockedUser);
		mockedUser.setActive(false);
		
		userService.loadUserByUsername(mockedUser.getUsername());
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testGetUserDetailsDtoNoUsernameFail() throws ParseException {
		User mockedUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findByUsername(mockedUser.getUsername())).thenReturn(null);
		userService.getUserDetailsDto(mockedUser.getUsername());
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testUpdatePasswordAdminNoUsernameFail() throws ParseException { 
		User mockedUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findByUsername(mockedUser.getUsername())).thenReturn(null);
		userService.updatePasswordAdmin(Mockito.any(PasswordUpdateAdminDTO.class), mockedUser.getUsername());
		
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testUpdatePasswordAdminNoUserIdFail() throws ParseException {
		User mockedUser = createMockUserAdminRole();
		PasswordUpdateAdminDTO mockPwdUpdateDto = new PasswordUpdateAdminDTO();
		
		mockPwdUpdateDto.setUserId("1266");
		
		Mockito.when(userRepository.findByUsername(mockedUser.getUsername())).thenReturn(mockedUser);
		Mockito.when(roleService.isUserAdmin(mockedUser)).thenReturn(Mockito.anyBoolean());
		Mockito.when(userRepository.findById(mockPwdUpdateDto.getUserId())).thenReturn(null);
		userService.updatePasswordAdmin(mockPwdUpdateDto, mockedUser.getUsername());
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testUpdatePasswordAdminAccessDeniedFail() throws ParseException {
		User mockedCurrentUser = createMockUserNonAdminRole();
		Optional<User> mockedUpdatedUser = Optional.of(createMockUserAdminRole());
		
		PasswordUpdateAdminDTO mockUpdateDto = new PasswordUpdateAdminDTO();
		mockUpdateDto.setUserId(mockedUpdatedUser.get().getId());
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(false);
		Mockito.when(userRepository.findById(mockUpdateDto.getUserId())).thenReturn(mockedUpdatedUser);
		userService.updatePasswordAdmin(mockUpdateDto, mockedCurrentUser.getUsername());
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testUpdatePasswordUserNoUsernameFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(null);
		userService.updatePasswordUser(Mockito.any(PasswordUpdateUserDTO.class), mockCurrentUser.getUsername());
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testUpdateUserNoCurrentFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(null);
		userService.updateUser(Mockito.any(UserDetailsDTO.class), mockCurrentUser.getUsername());
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public  void testUpdateUserNoUpdatedUserFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		UserDetailsDTO mockUserDetails = new UserDetailsDTO();
		mockUserDetails.setUsername("");
		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(mockCurrentUser);
		Mockito.when(userRepository.findByUsername(mockUserDetails.getUsername())).thenReturn(null);
		userService.updateUser(mockUserDetails, mockCurrentUser.getUsername());
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testUpdateUserAccessDeniedFail() throws ParseException {
		User mockCurrentUser = createMockUserAdminRole();
		User mockUpdatedUser = createMockUserNonAdminRole();
		UserDetailsDTO mockUserDetailsDto = new UserDetailsDTO();
		mockUserDetailsDto.setUsername(mockUpdatedUser.getUsername());
		mockUpdatedUser.setId(mockUpdatedUser.getId());
		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(mockCurrentUser);
		Mockito.when(userRepository.findByUsername(mockUserDetailsDto.getUsername())).thenReturn(mockUpdatedUser);
		userService.updateUser(mockUserDetailsDto, mockCurrentUser.getUsername());
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testUpdateUserAdminNoCurrentUserFail() throws ParseException {

		User mockCurrentUser = createMockUserNonAdminRole();
		UserDetailsDTO request = new UserDetailsDTO(mockCurrentUser);

		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(null);

		userService.updateUserAdmin(request, mockCurrentUser.getUsername());
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testUpdateUserAdminAccessDeniedFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		User mockUpdatedUser = createMockUserAdminRole();
		UserDetailsDTO mockUserDetailsDto = new UserDetailsDTO();
		mockUserDetailsDto.setUsername(mockUpdatedUser.getUsername());
		mockUpdatedUser.setId(mockUpdatedUser.getId());
		
		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(mockCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockCurrentUser)).thenReturn(false);
		Mockito.when(userRepository.findByUsername(mockUpdatedUser.getUsername())).thenReturn(mockUpdatedUser);
		
		userService.updateUserAdmin(mockUserDetailsDto, mockCurrentUser.getUsername());
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void testDeleteUserNoCurrentUserFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(null);
		userService.deleteUser(Mockito.anyString(), mockCurrentUser.getUsername());
	}
	
	
	@Test(expected = NoSuchElementException.class)
	public void testDeleteUserNoUserIdFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Optional<User> mockDeleteUser = Optional.of(createMockUserAdminRole());

		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(mockCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockCurrentUser)).thenReturn(false);
		Mockito.when(userRepository.findById(mockDeleteUser.get().getId())).thenReturn(Optional.empty());
		
		userService.deleteUser(mockDeleteUser.get().getId(), mockCurrentUser.getUsername());
	}
	
	@Test(expected = AccessDeniedException.class)
	public void testDeleteUserAccessDeniedFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Optional<User> mockDeleteUser = Optional.of(createMockUserAdminRole());

		Mockito.when(userRepository.findByUsername(mockCurrentUser.getUsername())).thenReturn(mockCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockCurrentUser)).thenReturn(false);
		Mockito.when(userRepository.findById(mockDeleteUser.get().getId())).thenReturn(mockDeleteUser);
		
		userService.deleteUser(mockDeleteUser.get().getId(), mockCurrentUser.getUsername());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testGetVendorDetailsNoUserIdFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findOneById(mockCurrentUser.getId())).thenReturn(null);
		userService.getVendorDetails(mockCurrentUser.getId());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testGetVendorDetailsNoPartnerFail() throws ParseException {
		User mockCurrentUser = createMockUserNonAdminRole();
		Mockito.when(userRepository.findOneById(mockCurrentUser.getId())).thenReturn(mockCurrentUser);
		Mockito.when(partnerRepository.findOneById(mockCurrentUser.getPartnerId())).thenThrow(new NoSuchElementException("PartnerId not found: " + mockCurrentUser.getPartnerId()));
		userService.getVendorDetails(mockCurrentUser.getId());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void decommissionUserNoDecommissionedUserFail() throws ParseException {
		User mockedCurrentUser = createMockUserNonAdminRole();
		User mockedDecommUser = createMockUserAdminRole();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findOneById(mockedDecommUser.getId())).thenReturn(null);
		
		userService.decommissionUser(mockedDecommUser.getId(), mockedCurrentUser.getUsername());
	}
	
	@Test(expected = AccessDeniedException.class)
	public void decommissionUserAccessDeniedFail() throws ParseException {
		User mockedCurrentUser = createMockUserNonAdminRole();
		User mockedDecommUser = createMockUserAdminRole();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findOneById(mockedDecommUser.getId())).thenReturn(mockedDecommUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(false);
		
		userService.decommissionUser(mockedDecommUser.getId(), mockedCurrentUser.getUsername());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void activateUserNoDecommissionedUserFail() throws ParseException {
		User mockedCurrentUser = createMockUserNonAdminRole();
		User mockedDecommUser = createMockUserAdminRole();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(userRepository.findOneById(mockedDecommUser.getId())).thenReturn(null);
		
		userService.activateUser(mockedDecommUser.getId(), mockedCurrentUser.getUsername());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testGetUserByIdIsAdminUserIdNotFoundFail() throws ParseException {
		User mockedCurrentUser = createMockUserAdminRole();
		User mockedSearchedUser = createMockUserNonAdminRole();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(true);
		Mockito.when(userRepository.findById(mockedSearchedUser.getId())).thenReturn(Optional.empty());
		
		userService.getUserById(mockedSearchedUser.getId(), mockedCurrentUser.getUsername());
	}
	
	@Test(expected = NoSuchElementException.class)
	public void testGetUserByIdNotAdminUserIdNotFoundFail() throws ParseException {
		User mockedCurrentUser = createMockUserAdminRole();
		User mockedSearchedUser = createMockUserNonAdminRole();
		
		Mockito.when(userRepository.findByUsername(mockedCurrentUser.getUsername())).thenReturn(mockedCurrentUser);
		Mockito.when(roleService.isUserAdmin(mockedCurrentUser)).thenReturn(false);
		Mockito.when(userRepository.findByIdAndPartnerId(mockedSearchedUser.getId(), mockedCurrentUser.getPartnerId())).thenReturn(Optional.empty());
		
		userService.getUserById(mockedSearchedUser.getId(), mockedCurrentUser.getUsername());
	}
	
	private Partner createMockPartner() {
		Partner mockPartner = new Partner();
		
		mockPartner.setId("1666");
		mockPartner.setKapschPartnerId("4466");
		mockPartner.setName("John DOe");
		
		return mockPartner;
	}

	private User createMockUserAdminRole() throws ParseException {

		User repoUser = new User();
		Role mockRole = new Role();

		mockRole.setCode("ROLE_ADMIN");
		mockRole.setCreatedAt(formatter.parse("2015-09-26T09:30:00.000+0000"));
		mockRole.setId("id");
		mockRole.setName("name");

		repoUser.setId("1278");
		repoUser.setFirstName("Digi");
		repoUser.setLastName("Toll");
		repoUser.setPartnerId("1666");
		repoUser.setPassword("Password123");
		repoUser.setUsername("digitoll@test.com");
		repoUser.setPosIds(Arrays.asList("6a7abd"));
		repoUser.setCreatedAt(formatter.parse("2015-09-26T09:30:00.000+0000"));
		repoUser.setUpdatedAt(formatter.parse("2015-09-30T09:30:00.000+0000"));
		repoUser.setRoles(Arrays.asList(mockRole));

		return repoUser;
	}

	private User createMockUserNonAdminRole() throws ParseException {

		User repoUser = new User();
		Role mockRole = new Role();

		mockRole.setCode("ROLE_USERS");
		mockRole.setCreatedAt(formatter.parse("2015-09-15T09:30:00.000+0000"));
		mockRole.setId("id");
		mockRole.setName("name");

		repoUser.setId("9999");
		repoUser.setFirstName("Digi");
		repoUser.setLastName("TollUser");
		repoUser.setPartnerId("1288");
		repoUser.setPassword("Password1666");
		repoUser.setUsername("digitolluser@test.com");
		repoUser.setPosIds(Arrays.asList("9ajg78"));
		repoUser.setCreatedAt(formatter.parse("2015-09-02T09:30:00.000+0000"));
		repoUser.setUpdatedAt(formatter.parse("2015-09-04T09:30:00.000+0000"));
		repoUser.setRoles(Arrays.asList(mockRole));

		return repoUser;
	}
	
	private User createMockUserNonAdminRole2() throws ParseException {

		User repoUser = new User();
		Role mockRole = new Role();

		mockRole.setCode("ROLE_USERS");
		mockRole.setCreatedAt(formatter.parse("2015-04-22T09:30:00.000+0000"));
		mockRole.setId("id");
		mockRole.setName("name");

		repoUser.setId("6666");
		repoUser.setFirstName("Argo");
		repoUser.setLastName("Talaminni");
		repoUser.setPartnerId("4646");
		repoUser.setPassword("8866");
		repoUser.setUsername("ArgoTalaminni@test.com");
		repoUser.setPosIds(Arrays.asList("j87sdA"));
		repoUser.setCreatedAt(formatter.parse("2015-04-22T09:30:00.000+0000"));
		repoUser.setUpdatedAt(formatter.parse("2015-09-04T09:30:00.000+0000"));
		repoUser.setRoles(Arrays.asList(mockRole));

		return repoUser;
	}

	private Optional<Role> fetchPartnerEmployeeRole() throws ParseException {

		Role mockRole = new Role();
		mockRole.setCode("ROLE_C2");
		mockRole.setName("C2");
		mockRole.setCreatedAt(formatter.parse("2015-09-15T09:30:00.000+0000"));
		mockRole.setId("1464");
		
		
		return Optional.of(mockRole);
	}
}
