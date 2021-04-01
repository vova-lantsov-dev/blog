package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_1.*
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.ExecBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_1.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2018_1.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Up'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Up")) {
    params {
        add {
            text("env.ASPNETCORE_ENVIRONMENT", "Production", readOnly = true, allowEmpty = true)
        }
        add {
            password("env.SENTRY_DSN", "credentialsJSON:205daec2-7af9-46f4-a515-9c9a90e027d0", display = ParameterDisplay.HIDDEN)
        }
        add {
            password("env.POSTGRES_PASSWORD", "credentialsJSON:1e25d30c-7993-4f20-8a26-42255dfc111a", display = ParameterDisplay.HIDDEN)
        }
    }

    expectSteps {
        exec {
            path = "build.sh"
            arguments = "Up --skip"
        }
    }
    steps {
        insert(0) {
            step {
                name = "Production docker-compose configuration"
                type = "CreateTextFile"
                param("system.dest.file", "%teamcity.build.checkoutDir%/src/docker-compose.override.yml")
                param("content", """
                    version: '3.8'
                    
                    services:
                      host:
                        volumes:
                          - /keys:/keys
                        environment:
                          - ASPNETCORE_ENVIRONMENT=%env.ASPNETCORE_ENVIRONMENT%
                """.trimIndent())
            }
        }
        insert(1) {
            step {
                name = "Production database settings"
                type = "CreateTextFile"
                enabled = false
                param("system.dest.file", "%teamcity.build.checkoutDir%/src/Server/dbsettings.Production.json")
                param("content", """
                    {
                        "ConnectionStrings": {
                            "BlogConnectionString": "Host=db-postgresql-fra1-35121-do-user-8845680-0.b.db.ondigitalocean.com;Port=25060;UserId=doadmin;Password=%env.POSTGRES_PASSWORD%;Database=blog;CommandTimeout=300;SslMode=Require;ClientCertificate=/keys/ca-certificate.crt;TrustServerCertificate=true"
                        }
                    }
                """.trimIndent())
            }
        }
        insert(2) {
            step {
                name = "Production application settings"
                type = "CreateTextFile"
                param("system.dest.file", "%teamcity.build.checkoutDir%/src/Server/appsettings.Production.json")
                param("content", """
                    {
                      "Logging": {
                        "LogLevel": {
                          "Default": "Information",
                          "Microsoft": "Warning",
                          "Microsoft.Hosting.Lifetime": "Information"
                        }
                      },
                      "IdentityServer": {
                        "Key": {
                          "Type": "Development"
                        }
                      }
                    }
                """.trimIndent())
            }
        }
        insert(3) {
            step {
                name = "Production sentry settings"
                type = "CreateTextFile"
                param("system.dest.file", "%teamcity.build.checkoutDir%/src/Server/sentrysettings.Production.json")
                param("content", """
                    {
                        "Sentry": {
                            "Dsn": "%env.SENTRY_DSN%"
                        }
                    }
                """.trimIndent())
            }
        }
        update<ExecBuildStep>(4) {
            clearConditions()
        }
    }
}
