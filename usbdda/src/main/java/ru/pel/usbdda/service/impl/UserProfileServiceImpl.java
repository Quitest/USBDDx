package ru.pel.usbdda.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pel.usbdda.entity.UserProfile;
import ru.pel.usbdda.repository.UserProfileRepository;
import ru.pel.usbdda.service.UserProfileService;

@Service
public class UserProfileServiceImpl implements UserProfileService {
    @Autowired
    UserProfileRepository repository;

    @Override
    public long save(UserProfile userProfile) {
        userProfile.getUsbDeviceList().forEach(usbDevice -> usbDevice.addUserProfile(userProfile));
        repository.save(userProfile);
        return userProfile.getId();
    }
}
