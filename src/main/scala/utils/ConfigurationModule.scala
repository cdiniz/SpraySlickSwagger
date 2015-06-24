package utils

import com.typesafe.config.{Config, ConfigFactory}

trait Configuration {
  def config: Config
}

trait ConfigurationModuleImpl extends Configuration {
  def config = ConfigFactory.load()
}