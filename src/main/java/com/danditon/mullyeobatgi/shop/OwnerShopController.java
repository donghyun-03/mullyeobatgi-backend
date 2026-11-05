package com.danditon.mullyeobatgi.shop;

import com.danditon.mullyeobatgi.auth.AuthUser;
import com.danditon.mullyeobatgi.user.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ① 사장님 가게 등록
 */
@RestController
@RequestMapping("/api/owner/shop")
@RequiredArgsConstructor
public class OwnerShopController {

	private final ShopService shopService;

	@GetMapping
	public ShopResponse myShop(@AuthenticationPrincipal AuthUser me) {
		me.requireRole(Role.OWNER);
		return shopService.myShop(me.id());
	}

	@PutMapping
	public ShopResponse save(@AuthenticationPrincipal AuthUser me, @Valid @RequestBody ShopRequest request) {
		me.requireRole(Role.OWNER);
		return shopService.save(me.id(), request);
	}

}
