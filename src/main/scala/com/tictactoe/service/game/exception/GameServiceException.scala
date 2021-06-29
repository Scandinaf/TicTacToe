package com.tictactoe.service.game.exception

trait GameServiceException extends RuntimeException {
  def message: String
}
