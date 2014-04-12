class StolenSessionsController < ApplicationController
  before_action :set_stolen_session, only: [:show, :edit, :update, :destroy]

  # GET /stolen_sessions
  # GET /stolen_sessions.json
  def index
    @stolen_sessions = StolenSession.all
  end

  # GET /stolen_sessions/1
  # GET /stolen_sessions/1.json
  def show
  end

  # GET /stolen_sessions/new
  def new
    @stolen_session = StolenSession.new
  end

  # GET /stolen_sessions/1/edit
  def edit
  end

  # POST /stolen_sessions
  # POST /stolen_sessions.json
  def create
    @stolen_session = StolenSession.new(stolen_session_params)

    respond_to do |format|
      if @stolen_session.save
        format.html { redirect_to @stolen_session, notice: 'Stolen session was successfully created.' }
        format.json { render :show, status: :created, location: @stolen_session }
      else
        format.html { render :new }
        format.json { render json: @stolen_session.errors, status: :unprocessable_entity }
      end
    end
  end

  # PATCH/PUT /stolen_sessions/1
  # PATCH/PUT /stolen_sessions/1.json
  def update
    respond_to do |format|
      if @stolen_session.update(stolen_session_params)
        format.html { redirect_to @stolen_session, notice: 'Stolen session was successfully updated.' }
        format.json { render :show, status: :ok, location: @stolen_session }
      else
        format.html { render :edit }
        format.json { render json: @stolen_session.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /stolen_sessions/1
  # DELETE /stolen_sessions/1.json
  def destroy
    @stolen_session.destroy
    respond_to do |format|
      format.html { redirect_to stolen_sessions_url }
      format.json { head :no_content }
    end
  end

  private
    # Use callbacks to share common setup or constraints between actions.
    def set_stolen_session
      @stolen_session = StolenSession.find(params[:id])
    end

    # Never trust parameters from the scary internet, only allow the white list through.
    def stolen_session_params
      params[:stolen_session]
    end
end
