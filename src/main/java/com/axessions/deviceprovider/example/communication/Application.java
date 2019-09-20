package com.axessions.deviceprovider.example.communication;

import com.axessions.deviceprovider.CommunicationException;
import com.axessions.deviceprovider.DeviceProviderClient;
import com.axessions.deviceprovider.DeviceProviderClientException;
import com.axessions.deviceprovider.DeviceProviderConfig;
import com.axessions.deviceprovider.DeviceProviderListener;
import com.axessions.deviceprovider.RemoteDeviceProviderClient;
import com.axessions.deviceprovider.VaultStorageHandler;
import com.axessions.deviceprovider.dto.ActuatorCreateRequest;
import com.axessions.deviceprovider.dto.AxessionsConfiguration;
import com.axessions.deviceprovider.dto.ChangeAction;
import com.axessions.deviceprovider.dto.DeviceChangedNotification;
import com.axessions.deviceprovider.dto.DeviceProviderChangedNotification;
import com.axessions.deviceprovider.dto.DeviceRegistrationRequest;
import com.axessions.deviceprovider.dto.DeviceRegistrationResponse;
import com.axessions.deviceprovider.dto.edge.RecEdgeMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Application implements DeviceProviderListener {

  private ObjectMapper objectMapper;

  private DeviceProviderClient client;

  public static void main(String[] args) {
    var app = new Application();
    app.start();
  }

  private void start() {
    try {
      objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ"));

      var configuration = getConfiguration();
      client = new RemoteDeviceProviderClient();
      client.setListener(this);
      client.start(configuration);
    } catch (DeviceProviderClientException e) {
      e.printStackTrace();
    }
  }

  private DeviceProviderConfig getConfiguration() {
    String vaultPassword = "123456";
    var axessionsConfiguration = getAxessionsConfiguration();
    var vaultStorageHandler = new MyVaultStorageHandler();
    var configuration = new DeviceProviderConfig(vaultPassword, axessionsConfiguration,
        vaultStorageHandler);
    return configuration;
  }

  private AxessionsConfiguration getAxessionsConfiguration() {

    try {
      var jsonFile = getClass().getClassLoader().getResource("axs.json");
      return objectMapper.readValue(jsonFile, AxessionsConfiguration.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public void onDeviceProviderChanged(
      DeviceProviderChangedNotification deviceProviderChangedNotification) {
    System.out.println("#onDeviceProviderChanged");
    try {
      System.out.println(objectMapper.writeValueAsString(deviceProviderChangedNotification));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onDeviceChanged(DeviceChangedNotification deviceChangedNotification) {
    System.out.println("#onDeviceChanged");
    try {
      System.out.println(objectMapper.writeValueAsString(deviceChangedNotification));

      if (deviceChangedNotification.getAction().equals(ChangeAction.CREATED) ||
          deviceChangedNotification.getAction().equals(ChangeAction.UPDATED)) {
        var registration = new DeviceRegistrationRequest();
        registration.setPopularName("Endpoint c617b656-0b96-431a-9d12-6da52aa1d382");
        registration.setLittera("Endpoint c617b656-0b96-431a-9d12-6da52aa1d382");
        registration.setAliasId(deviceChangedNotification.getAliasId());

        var source = new HashMap<String, String>();
        source.put("My key", "My value");
        registration.setSource(source);

        var actuator1 = new ActuatorCreateRequest();
        actuator1.setPopularName("Actuator 1");
        actuator1.setLittera("Actuator 1");
        actuator1.setAliasId("c617b656-0b96-431a-9d12-6da52aa1d382/actuator1");
        registration.setActuators(List.of(actuator1));

        client.registerDevice(registration);
      }

    } catch (JsonProcessingException | CommunicationException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onDeviceRegistrationResponse(DeviceRegistrationResponse deviceRegistrationResponse) {
    System.out.println("#onDeviceRegistrationResponse");
    try {
      System.out.println(objectMapper.writeValueAsString(deviceRegistrationResponse));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onEdgeMessage(RecEdgeMessage recEdgeMessage) {
    System.out.println("#onEdgeMessage");
    try {
      System.out.println(objectMapper.writeValueAsString(recEdgeMessage));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  class MyVaultStorageHandler implements VaultStorageHandler {

    @Override
    public Optional<byte[]> load(UUID uuid) {
      try {
        return Optional.of(Files.readAllBytes(Paths.get("./" + uuid.toString() + ".vault")));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return Optional.empty();
    }

    @Override
    public void save(UUID uuid, byte[] bytes) {
      try {
        Files.write(Paths.get("./" + uuid.toString() + ".vault"), bytes);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void delete(UUID uuid) {
      try {
        Files.delete(Paths.get("./" + uuid.toString() + ".vault"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}