package groom._55.security;

import groom._55.entity.User;
import groom._55.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 사용자명(username)을 기준으로 DB에서 사용자 정보를 불러와 UserDetails로 반환합니다.
     * JWT의 subject에 해당하는 값으로 사용자를 찾습니다.
     * @param username JWT의 subject에 해당하는 사용자 식별자 (카카오 ID)
     * @return UserDetails 객체
     * @throws UsernameNotFoundException 사용자가 DB에 존재하지 않을 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 데이터베이스에서 사용자 이름(여기서는 카카오 ID)으로 사용자를 찾습니다.
        // `userRepository`는 실제 프로젝트에 맞게 수정해야 합니다.
        User user = userRepository.findByPassword(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 데이터베이스에서 로드한 사용자 정보를 기반으로 UserDetails 객체를 생성합니다.
        // 스프링 시큐리티의 User 클래스를 사용하며, 역할(Role) 정보는 임시로 비워둡니다.
        // User.getPassword()는 String.valueOf(userInfo.getId())가 됩니다.
        return new org.springframework.security.core.userdetails.User(
                user.getName(), // 사용자명 (카카오 닉네임)
                user.getPassword(), // 사용자의 식별자 (카카오 ID)
                Collections.emptyList() // 사용자 권한 (예제에서는 비어있음)
        );
    }
}