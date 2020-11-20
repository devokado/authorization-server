FROM quay.io/keycloak/keycloak:latest

COPY keycloak-sms-authenticator-sns-2.0.0.jar /opt/jboss/keycloak/standalone/deployments/

COPY templates/sms-validation.ftl /opt/jboss/keycloak/themes/base/login/
COPY templates/sms-validation-error.ftl /opt/jboss/keycloak/themes/base/login/
COPY templates/sms-validation-mobile-number.ftl /opt/jboss/keycloak/themes/base/login/

CMD cat templates/messages/messages_en.properties >> /opt/jboss/keycloak/themes/base/login/messages/messages_en.properties